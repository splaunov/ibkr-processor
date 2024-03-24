package me.splaunov.ibkrprocessor.processor

import me.splaunov.ibkrprocessor.data.InstrumentInformation
import me.splaunov.ibkrprocessor.data.SellingDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.reader.Acquisition
import me.splaunov.ibkrprocessor.reader.CorporateAction
import me.splaunov.ibkrprocessor.reader.StockSplit

class OpenPositions(
    private val instrumentInformation: Map<String, InstrumentInformation>,
) {
    private val positions = mutableMapOf<String, OpenPosition>()

    fun getPositions(): Map<String, OpenPosition> = positions

    private fun processSplit(split: StockSplit) {
        val position = positions[getSecurityId(split.symbol)]
            ?: error("Could not process split as position is not open: ${split.symbol}")
        position.processSplit(split)
    }

    fun processPurchase(trade: TradeOrder) {
        val securityId = getSecurityId(trade.symbol)
        val position = positions[securityId] ?: OpenPosition(trade.symbol, trade.currency)
        position.processPurchase(trade)
        positions[securityId] = position
    }

    fun processSelling(trade: TradeOrder): SellingDetails {
        val securityId = getSecurityId(trade.symbol)
        val position = positions[securityId]
            ?: error("Could not process selling as position is not open: ${trade.symbol}")
        val sellingDetails = position.processSelling(trade)
        if (position.isSold()) {
            positions.remove(securityId)
        }
        return sellingDetails
    }

    private fun processAcquisition(acquisition: Acquisition) {
        val position = positions.remove(getSecurityId(acquisition.firstSymbol))
            ?: error("Could not process acquisition as position is not open: ${acquisition.firstSymbol}")
        position.processAcquisition(acquisition)
        positions[getSecurityId(position.symbol)] = position
    }

    fun processCorporateAction(action: CorporateAction) {
        when (action.details) {
            is StockSplit -> processSplit(action.details)
            is Acquisition -> processAcquisition(action.details)
        }
    }

    private fun getSecurityId(symbol: String): String =
        instrumentInformation[symbol]?.securityId ?: error("Instrument information not found: $symbol")

}
