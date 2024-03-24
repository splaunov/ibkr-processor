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
        require(!trade.isSelling()) { "Selling passed to processPurchase method" }
        validateTradeOrder(trade)

        purchases.add(PurchaseDetails(trade, 0F))
        lastProcessedOperationDate = trade.date
    }

    fun processSelling(trade: TradeOrder): SellingDetails {
        require(trade.isSelling()) { "Purchase passed to processSelling method" }
        validateTradeOrder(trade)

        val purchaseOperationsDetails = mutableListOf<PurchaseDetails>()
        var quantitySold = -trade.quantity
        var purchase: PurchaseDetails = purchases.peek()

        do {
            if (trade.date < purchase.purchaseOrder.date) error(
                """Selling is earlier than purchase. Check the source data.
                        |   Purchase: $purchase
                        |   Selling: $trade""".trimMargin()
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
                purchase = purchases.peek() ?: if (quantitySold > 0) {
                    error(
                        """Selling is larger than purchase.
                            |   Purchase: $purchase
                            |   Selling: $trade""".trimMargin()
                    )
                } else {
                    break
                }
            }
        } while (quantitySold > 0)

        lastProcessedOperationDate = trade.date

        return SellingDetails(trade, purchaseOperationsDetails)
    }

    fun processSplit(split: StockSplit) {
        require(symbol == split.symbol) { "Expected $symbol but got ${split.symbol}" }
        require(lastProcessedOperationDate < split.date) { "Split date is earlier than operation processed previously" }

        purchases.forEach {
            it.purchaseOrder.quantity *= split.multiplier
            it.purchaseOrder.price /= split.multiplier
            it.quantitySold *= split.multiplier
        }

        lastProcessedOperationDate = split.date
    }

    private fun validateTradeOrder(trade: TradeOrder) {
        require(currency == trade.currency) { "Expected $currency but got ${trade.currency}" }
        require(lastProcessedOperationDate < trade.date) {
            "Trade order date is earlier than operation processed previously"
        }
    }

    fun processAcquisition(acquisition: Acquisition) {
        require(symbol == acquisition.firstSymbol) {
            "Expected $symbol but got ${acquisition.firstSymbol}"
        }
        require(lastProcessedOperationDate < acquisition.date) {
            "Acquisition date is earlier than operation processed previously"
        }

        purchases.forEach {
            val k = acquisition.secondQuantity / acquisition.firstQuantity
            it.purchaseOrder.quantity *= -k
            it.quantitySold *= -k
            it.purchaseOrder.price += acquisition.firstProceeds / acquisition.firstQuantity
            it.purchaseOrder.price /= -k
            it.purchaseOrder.symbol = acquisition.secondSymbol
        }

        symbol = acquisition.secondSymbol
        lastProcessedOperationDate = acquisition.date
    }

    fun isSold() =
        purchases.isEmpty()
            || purchases.size == 1 && purchases.peek().quantitySold == purchases.peek().purchaseOrder.quantity
}
