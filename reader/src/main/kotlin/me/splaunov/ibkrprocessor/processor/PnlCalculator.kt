package me.splaunov.ibkrprocessor.processor

import jakarta.inject.Singleton
import me.splaunov.ibkrprocessor.data.InstrumentInformation
import me.splaunov.ibkrprocessor.data.SellingDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.data.toLocalDate
import me.splaunov.ibkrprocessor.reader.CorporateAction
import me.splaunov.ibkrprocessor.reader.CurrencyRatesProvider

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
    fun calculateRealizedPnlRub(sellOperations: List<SellingDetails>): Map<Int, Float> {
        val result = mutableMapOf(2020 to 0f, 2021 to 0f, 2022 to 0f, 2023 to 0f)
        result.forEach { (year, _) ->
            result[year] =
                sellOperations.filter { it.sellOrder.date.toLocalDate().year == year }.fold(0f) { acc1, sellOp ->
                    acc1 +
                            sellOp.sellOrder.getProceedsBaseCurrency() * currencyRatesProvider.getRate(
                        sellOp.sellOrder.date.toLocalDate(),
                        sellOp.sellOrder.currency
                    ) +
                            sellOp.purchases.fold(0f) { acc2, purchaseOp ->
                                acc2 + purchaseOp.purchaseOrder.getProceedsBaseCurrency(
                                    purchaseOp.quantitySold
                                ) * currencyRatesProvider.getRate(
                                    purchaseOp.purchaseOrder.date.toLocalDate(),
                                    purchaseOp.purchaseOrder.currency
                                )
                            }
                }
        }
        return result
    }

    fun getSellingDetails(
        trades: List<TradeOrder>,
        actions: List<CorporateAction>,
        instrumentInformation: Map<String, InstrumentInformation>
    ): List<SellingDetails> {
        val tradesIterator = trades.sortedBy { it.date }.iterator()
        val actionsIterator = actions.sortedBy { it.date }.iterator()
        val sellingDetailsList = mutableListOf<SellingDetails>()
        val openPositions = OpenPositions(instrumentInformation)

        var nextAction = if (actionsIterator.hasNext()) actionsIterator.next() else null

        while (tradesIterator.hasNext()) {
            val trade = tradesIterator.next()
            while (nextAction != null && trade.date > nextAction.date) {
                openPositions.processCorporateAction(nextAction)
                nextAction = if (actionsIterator.hasNext()) actionsIterator.next() else null
            }
            if (trade.isSelling()) {
                val sellingDetails = openPositions.processSelling(trade)
                sellingDetailsList.add(sellingDetails)
                continue
            }
            openPositions.processPurchase(trade)
        }

        return sellingDetailsList
    }

}
