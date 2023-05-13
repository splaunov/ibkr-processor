package me.splaunov.ibkrprocessor.processor

import io.kotest.matchers.shouldBe
import me.splaunov.ibkrprocessor.data.TradeOrder
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.Instant

class OpenPositionsTest {

    @Test
    fun processSplit() {
    }

    @Test
    fun processPurchase() {
        val openPositions = OpenPositions()

        openPositions.processPurchase(TradeOrder("TSLA", "USD", Instant.now(), 10F, 100F, 0F))
        openPositions.processPurchase(TradeOrder("TSLA", "USD", Instant.now(), 2F, 110F, 0F))

        val positionsList = openPositions.getPositions()
        positionsList.size shouldBe 1
        positionsList["TSLA"] shouldBe OpenPosition("TSLA", "USD")
    }

    @Test
    fun processSelling() {
        val openPositions = OpenPositions()

        openPositions.processPurchase(TradeOrder("TSLA", "USD", Instant.now(), 10F, 100F, 0F))
        val sellingDetails = openPositions.processSelling(TradeOrder("TSLA", "USD", Instant.now(), -2F, 110F, 0F))

        val positionsList = openPositions.getPositions()
        positionsList.size shouldBe 1
        positionsList["TSLA"] shouldBe OpenPosition("TSLA", "USD")
    }
}