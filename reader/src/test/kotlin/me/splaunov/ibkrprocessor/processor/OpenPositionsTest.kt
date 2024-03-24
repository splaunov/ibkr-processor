package me.splaunov.ibkrprocessor.processor

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import me.splaunov.ibkrprocessor.data.InstrumentInformation
import me.splaunov.ibkrprocessor.data.TradeOrder
import java.time.Instant

class OpenPositionsTest : StringSpec({
    withData(
        TestParams(
            "two buys", false,
            mapOf("001" to OpenPosition("TSLA", "USD")),
            mapOf("TSLA" to InstrumentInformation("TSLA", "001")),
            listOf(
                TradeOrder("TSLA", "USD", Instant.now(), 10F, 100F, 0F),
                TradeOrder("TSLA", "USD", Instant.now(), 2F, 110F, 0F),
            )
        ),
        TestParams(
            "buy and sell", false,
            mapOf("001" to OpenPosition("TSLA", "USD")),
            mapOf("TSLA" to InstrumentInformation("TSLA", "001")),
            listOf(
                TradeOrder("TSLA", "USD", Instant.now(), 10F, 100F, 0F),
                TradeOrder("TSLA", "USD", Instant.now().plusSeconds(60), -2F, 110F, 0F),
            )
        ),
        TestParams(
            "sell more than bought", true,
            mapOf("001" to OpenPosition("A", "USD")),
            mapOf("A" to InstrumentInformation("A", "001")),
            listOf(
                TradeOrder("A", "USD", Instant.now(), 1F, 100F, 0F),
                TradeOrder("A", "USD", Instant.now().plusSeconds(60), -2F, 110F, 0F),
            )
        ),
        TestParams(
            "buy, change symbol, sell partly", false,
            mapOf("001" to OpenPosition("A", "USD")),
            mapOf(
                "A" to InstrumentInformation("A", "001"),
                "B" to InstrumentInformation("B", "001"),
            ),
            listOf(
                TradeOrder("A", "USD", Instant.now(), 4F, 253F, 0F),
                TradeOrder("B", "USD", Instant.now().plusSeconds(2), -2F, 189F, 0F),
            )
        ),
        TestParams(
            "buy, change symbol, buy with new symbol and then sell all", false,
            mapOf(),
            mapOf(
                "FB" to InstrumentInformation("FB", "001"),
                "META" to InstrumentInformation("META", "001"),
            ),
            listOf(
                TradeOrder("FB", "USD", Instant.now(), 4F, 253F, 0F),
                TradeOrder("META", "USD", Instant.now().plusSeconds(1), 6F, 98F, 0F),
                TradeOrder("META", "USD", Instant.now().plusSeconds(2), -10F, 189F, 0F),
            )
        ),
    ) { (_, expectException, expectOpenPositions, instrumentInformation, tradeOrders) ->
        val openPositions = OpenPositions(instrumentInformation)
        val doTest = fun() {
            tradeOrders.forEach {
                if (it.isSelling()) {
                    openPositions.processSelling(it)
                } else {
                    openPositions.processPurchase(it)
                }
            }
        }

        if (expectException) {
            shouldThrowAny(doTest)
        } else {
            doTest()

            openPositions.getPositions() shouldBe expectOpenPositions
        }
    }
}) {
    data class TestParams(
        val testName: String,
        val expectException: Boolean,
        val expectOpenPositions: Map<String, OpenPosition>,
        val instrumentInformation: Map<String, InstrumentInformation>,
        val tradeOrders: List<TradeOrder>,
    ) : WithDataTestName {
        override fun dataTestName() = testName
    }
}
