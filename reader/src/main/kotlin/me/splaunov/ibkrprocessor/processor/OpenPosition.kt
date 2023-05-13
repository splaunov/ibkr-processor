package me.splaunov.ibkrprocessor.processor

import me.splaunov.ibkrprocessor.data.PurchaseDetails
import me.splaunov.ibkrprocessor.data.SellingDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.reader.Acquisition
import me.splaunov.ibkrprocessor.reader.StockSplit
import java.time.Instant
import java.util.LinkedList

data class OpenPosition(
    var symbol: String,
    val currency: String,
) {
    val purchases = LinkedList<PurchaseDetails>()
    var lastProcessedOperationDate: Instant = Instant.EPOCH
        private set

    fun processPurchase(trade: TradeOrder) {
        if (trade.isSelling()) {
            throw IllegalArgumentException("Selling passed to processPurchase method")
        }
        validateTradeOrder(trade)

        purchases.add(PurchaseDetails(trade, 0F))
        lastProcessedOperationDate = trade.date
    }

    fun processSelling(trade: TradeOrder): SellingDetails {
        if (!trade.isSelling()) {
            throw IllegalArgumentException("Purchase passed to processSelling method")
        }
        validateTradeOrder(trade)

        val purchaseOperationsDetails = mutableListOf<PurchaseDetails>()
        var quantitySold = -trade.quantity
        var purchase: PurchaseDetails = purchases.peek()

        do {
            if (trade.date < purchase.purchaseOrder.date) throw IllegalStateException(
                """Selling is earlier than purchase. Check the source data.
                        |   Selling: $trade
                        |   Purchase: $purchase""".trimMargin()
            )
            val s: Float
            val purchaseRemainder = purchase.purchaseOrder.quantity - purchase.quantitySold
            if (purchaseRemainder <= quantitySold) {
                s = purchaseRemainder
                quantitySold -= purchaseRemainder
                purchase.quantitySold = purchase.purchaseOrder.quantity
            } else {
                s = quantitySold
                purchase.quantitySold += quantitySold
                quantitySold = 0F
            }

            purchaseOperationsDetails.add(
                PurchaseDetails(
                    purchase.purchaseOrder.copy(),
                    s
                )
            )
            if (purchase.purchaseOrder.quantity == purchase.quantitySold) {
                purchases.remove()
                purchase = purchases.peek() ?: break
            }
        } while (quantitySold > 0)

        lastProcessedOperationDate = trade.date

        return SellingDetails(trade, purchaseOperationsDetails)
    }

    fun processSplit(split: StockSplit) {
        if (symbol != split.symbol) {
            throw IllegalArgumentException("Expected $symbol but got ${split.symbol}")
        }
        if (lastProcessedOperationDate >= split.date) {
            throw IllegalStateException("Split date is earlier than operation processed previously")
        }

        purchases.forEach {
            it.purchaseOrder.quantity *= split.multiplier
            it.purchaseOrder.price /= split.multiplier
            it.quantitySold *= split.multiplier
        }

        lastProcessedOperationDate = split.date
    }

    private fun validateTradeOrder(trade: TradeOrder) {
        if (symbol != trade.symbol) {
            throw IllegalArgumentException("Expected $symbol but got ${trade.symbol}")
        }
        if (currency != trade.currency) {
            throw IllegalArgumentException("Expected $currency but got ${trade.currency}")
        }
        if (lastProcessedOperationDate >= trade.date) {
            throw IllegalStateException("Trade order date is earlier than operation processed previously")
        }
    }

    fun processAcquisition(acquisition: Acquisition) {
        if (symbol != acquisition.firstSymbol) {
            throw IllegalArgumentException("Expected $symbol but got ${acquisition.firstSymbol}")
        }
        if (lastProcessedOperationDate >= acquisition.date) {
            throw IllegalStateException("Acquisition date is earlier than operation processed previously")
        }

        purchases.forEach {
            val k = acquisition.secondQuantity / acquisition.firstQuantity
            it.purchaseOrder.quantity *= -k
            it.quantitySold *= -k
            it.purchaseOrder.price += acquisition.firstProceeds/acquisition.firstQuantity
            it.purchaseOrder.price /= -k
            it.purchaseOrder.symbol = acquisition.secondSymbol
        }

        symbol = acquisition.secondSymbol
        lastProcessedOperationDate = acquisition.date
    }
}
