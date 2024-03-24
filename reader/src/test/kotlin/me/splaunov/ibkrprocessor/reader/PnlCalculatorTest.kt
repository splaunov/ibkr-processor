package me.splaunov.ibkrprocessor.reader

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import me.splaunov.ibkrprocessor.data.InstrumentInformation
import me.splaunov.ibkrprocessor.data.PurchaseDetails
import me.splaunov.ibkrprocessor.data.SellingDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.processor.PnlCalculator
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant

class PnlCalculatorTest {
    private val currencyRatesProvider = mockk<CurrencyRatesProvider>().apply {
        every { getRate(any(), any()) } returns 70f
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun calculateRealizedPnlRub(
        @Suppress("UNUSED_PARAMETER") caseDescription: String,
        sellOperations: List<SellingDetails>, expected: Float,
    ) {
        val actual = PnlCalculator(currencyRatesProvider).calculateRealizedPnlRub(sellOperations)

        actual[2020] shouldBe expected
    }

    @Suppress("unused")
    private fun calculateRealizedPnlRub() = listOf<Arguments>(
        Arguments.of(
            "two symbols",
            listOf(
                sellOperationDetails(
                    "AAPL", 20, -6F, 100F,
                    purchaseOperationDetails("AAPL", 0, 5F, 5F),
                    purchaseOperationDetails("AAPL", 10, 4F, 1F),
                ),
                sellOperationDetails(
                    "AMZN", 30, -1F, 100F,
                    purchaseOperationDetails("AMZN", 0, 10F, 1F)
                )
            ),
            -(-6 * 100f + 1f) * 70f - (5 * 100f + 1f) * 70f - (4 * 100f + 1f) * 70f * 1 / 4 +
                -(-1 * 100f + 1f) * 70f - (10 * 100f + 1f) * 70f * 1 / 10
        ),
    )

    private fun tradeUsd(
        symbol: String,
        tradeTimeInSeconds: Int,
        quantity: Float,
        price: Float = 100F,
    ) = TradeOrder(
        symbol,
        "USD",
        Instant.parse("2020-08-20T00:00:00Z").plusSeconds(tradeTimeInSeconds.toLong()),
        quantity,
        price,
        -1f
    )

    private fun stockSplitUsd(
        symbol: String,
        splitTimeInSeconds: Int,
        multiplier: Int,
    ) = CorporateAction(
        StockSplit(
            symbol,
            Instant.parse("2020-08-20T00:00:00Z").plusSeconds(splitTimeInSeconds.toLong()),
            multiplier
        )
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun getSellDetails(
        @Suppress("UNUSED_PARAMETER") caseDescription: String,
        trades: List<TradeOrder>,
        actions: List<CorporateAction>,
        istrumentInformation: Map<String, InstrumentInformation>,
        expected: List<SellingDetails>,
    ) {
        val actual = PnlCalculator(currencyRatesProvider).getSellingDetails(
            trades,
            actions,
            istrumentInformation
        )

        actual shouldBe expected
    }

    @Suppress("unused")
    private fun getSellDetails() = listOf<Arguments>(
        Arguments.of(
            "simple buy and sell",
            listOf(
                tradeUsd("AAPL", 0, 5F),
                tradeUsd("AAPL", 10, -5F),
            ),
            listOf<CorporateAction>(),
            mapOf("AAPL" to InstrumentInformation("AAPL", "001")),
            listOf(
                sellOperationDetails(
                    "AAPL", 10, -5F, 100F,
                    purchaseOperationDetails("AAPL", 0, 5F, 5F)
                )
            )
        ),
        Arguments.of(
            "partial sell",
            listOf(
                tradeUsd("AAPL", 0, 5F),
                tradeUsd("AAPL", 10, 4F),
                tradeUsd("AAPL", 20, -6F),
            ),
            listOf<CorporateAction>(),
            mapOf("AAPL" to InstrumentInformation("AAPL", "001")),
            listOf(
                sellOperationDetails(
                    "AAPL", 20, -6F, 100F,
                    purchaseOperationDetails("AAPL", 0, 5F, 5F),
                    purchaseOperationDetails("AAPL", 10, 4F, 1F),
                )
            )
        ),
        Arguments.of(
            "two symbols",
            listOf(
                tradeUsd("AAPL", 0, 5F),
                tradeUsd("AAPL", 10, 4F),
                tradeUsd("AAPL", 20, -6F),
                tradeUsd("AMZN", 0, 10F),
                tradeUsd("AMZN", 30, -1F),
            ),
            listOf<CorporateAction>(),
            mapOf(
                "AAPL" to InstrumentInformation("AAPL", "001"),
                "AMZN" to InstrumentInformation("AMZN", "002"),
            ),
            listOf(
                sellOperationDetails(
                    "AAPL", 20, -6F, 100F,
                    purchaseOperationDetails("AAPL", 0, 5F, 5F),
                    purchaseOperationDetails("AAPL", 10, 4F, 1F),
                ),
                sellOperationDetails(
                    "AMZN", 30, -1F, 100F,
                    purchaseOperationDetails("AMZN", 0, 10F, 1F)
                )
            )
        ),
        Arguments.of(
            "with split",
            listOf(
                tradeUsd("TSLA", 0, 1F, 100F),
                tradeUsd("TSLA", 20, 5F, 22F),
                tradeUsd("TSLA", 30, -10F, 25F),
            ),
            listOf<CorporateAction>(
                stockSplitUsd("TSLA", 10, 5)
            ),
            mapOf("TSLA" to InstrumentInformation("TSLA", "001")),
            listOf(
                sellOperationDetails(
                    "TSLA", 30, -10F, 25F,
                    purchaseOperationDetails("TSLA", 0, 5F, 5F, 20F),
                    purchaseOperationDetails("TSLA", 20, 5F, 5F, 22F),
                )
            )
        ),
    )

    private fun sellOperationDetails(
        symbol: String,
        sellTimeInSeconds: Int,
        quantity: Float,
        price: Float,
        vararg purchaseDetails: PurchaseDetails,
    ): SellingDetails = SellingDetails(
        tradeUsd(symbol, sellTimeInSeconds, quantity, price),
        purchaseDetails.toList()
    )

    private fun purchaseOperationDetails(
        symbol: String,
        purchaseTimeInSeconds: Int,
        quantity: Float,
        quantitySold: Float,
        purchasePrice: Float = 100F,
    ): PurchaseDetails =
        PurchaseDetails(
            tradeUsd(symbol, purchaseTimeInSeconds, quantity, purchasePrice),
            quantitySold
        )
}