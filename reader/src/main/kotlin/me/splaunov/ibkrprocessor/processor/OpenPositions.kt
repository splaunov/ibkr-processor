package me.splaunov.ibkrprocessor.processor

import me.splaunov.ibkrprocessor.data.SellingDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.reader.Acquisition
import me.splaunov.ibkrprocessor.reader.CorporateAction
import me.splaunov.ibkrprocessor.reader.StockSplit

class OpenPositions {
    private val positions = mutableMapOf<String, OpenPosition>()

    fun getPositions(): Map<String, OpenPosition> = positions
    private fun processSplit(split: StockSplit) {
        val position = positions[split.symbol]
            ?: throw IllegalStateException("Could not process split as position is not open: ${split.symbol}")
        position.processSplit(split)
    }

    fun processPurchase(trade: TradeOrder) {
        val position = positions[trade.symbol] ?: OpenPosition(trade.symbol, trade.currency)
        position.processPurchase(trade)
        positions[trade.symbol] = position
    }

    fun processSelling(trade: TradeOrder): SellingDetails {
        val position = positions[trade.symbol]
            ?: throw IllegalStateException("Could not process selling as position is not open: ${trade.symbol}")
        return position.processSelling(trade)
    }

    private fun processAcquisition(acquisition: Acquisition) {
        val position = positions.remove(acquisition.firstSymbol)
            ?: throw IllegalStateException("Could not process acquisition as position is not open: ${acquisition.firstSymbol}")
        position.processAcquisition(acquisition)
        positions[position.symbol] = position
    }

    fun processCorporateAction(action: CorporateAction) {
        when (action.details) {
            is StockSplit -> processSplit(action.details)
            is Acquisition -> processAcquisition(action.details)
        }
    }
}