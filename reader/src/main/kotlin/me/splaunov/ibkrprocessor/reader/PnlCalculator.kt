package me.splaunov.ibkrprocessor.reader

import me.splaunov.ibkrprocessor.data.PurchaseOperationDetails
import me.splaunov.ibkrprocessor.data.SellOperationDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import jakarta.inject.Singleton

/**
 * Processes list of trade orders and calculates PnL.
 *
 */
@Singleton
class PnlCalculator(private val currencyRatesProvider: CurrencyRatesProvider) {

    /**
     * Calculates realized PnL in RUB based on the FIFO method.
     *
     * @return Calculated PnL in RUB
     */
    fun calculateRealizedPnlRub(sellOperations: List<SellOperationDetails>): Float =
        sellOperations.fold(0f) { acc1, sellOp ->
            acc1 +
                    sellOp.sellOrder.getProceedsBaseCurrency() * sellOp.currencyRate +
                    sellOp.purchases.fold(0f) { acc2, purchaseOp ->
                        acc2 + purchaseOp.purchaseOrder.getProceedsBaseCurrency(
                            purchaseOp.quantitySold
                        ) * purchaseOp.currencyRate
                    }
        }

    fun getSellDetails(trades: List<TradeOrder>): List<SellOperationDetails> =
        trades.groupBy { it.symbol }.values.flatMap { oneSymbolTrades ->
            val (sales, purchases) = oneSymbolTrades.sortedBy { it.date }.partition { it.isSale() }

            val purchasesIterator = purchases.iterator()

            var purchaseRemainder = 0
            var purchase: TradeOrder? = null
            sales.map { sale ->
                var saleQuantity = -sale.quantity
                val purchaseOperationsDetails = mutableListOf<PurchaseOperationDetails>()
                do {
                    if (purchaseRemainder == 0) {
                        purchase = purchasesIterator.next()
                        purchaseRemainder = purchase!!.quantity
                    }
                    if (sale.date < purchase!!.date) throw IllegalStateException(
                        """Sale is earlier than purchase. Check the source data.
                        |   Sale: $sale
                        |   Purchase: $purchase""".trimMargin()
                    )
                    val quantitySold: Int
                    if (purchaseRemainder <= saleQuantity) {
                        quantitySold = purchaseRemainder
                        saleQuantity -= purchaseRemainder
                        purchaseRemainder = 0
                    } else {
                        quantitySold = saleQuantity
                        purchaseRemainder -= saleQuantity
                        saleQuantity = 0
                    }

                    purchaseOperationsDetails.add(
                        PurchaseOperationDetails(
                            purchase!!,
                            currencyRatesProvider.getRate(purchase!!.date),
                            quantitySold
                        )
                    )
                } while (saleQuantity > 0)


                SellOperationDetails(
                    sale,
                    currencyRatesProvider.getRate(sale.date),
                    purchaseOperationsDetails
                )

            }
        }

}

