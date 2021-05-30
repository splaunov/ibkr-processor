package me.splaunov.ibkrprocessor.reader

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import me.splaunov.ibkrprocessor.data.PurchaseOperationDetails
import me.splaunov.ibkrprocessor.data.SellOperationDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

class PnlCalculatorTest {
    private val currencyRatesProvider = mockk<CurrencyRatesProvider>().apply {
        every { getRate(any()) } returns 70f
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun calculateRealizedPnlRub(
        @Suppress("UNUSED_PARAMETER") caseDescription: String,
        sellOperations: List<SellOperationDetails>, expected: Float
    ) {
        val actual = PnlCalculator(currencyRatesProvider).calculateRealizedPnlRub(sellOperations)

        actual shouldBe expected
    }

    @Suppress("unused")
    private fun calculateRealizedPnlRub() = listOf<Arguments>(
        Arguments.of(
            "two symbols",
            listOf(
                sellOperationDetails(
                    "AAPL", "2020-12-10", -6,
                    purchaseOperationDetails("AAPL", "2020-12-05", 5, 5),
                    purchaseOperationDetails("AAPL", "2020-12-06", 4, 1),
                ),
                sellOperationDetails(
                    "AMZN", "2020-12-11", -1,
                    purchaseOperationDetails("AMZN", "2020-12-05", 10, 1)
                )
            ),
            -(-6 * 100f + 1f) * 70f - (5 * 100f + 1f) * 70f - (4 * 100f + 1f) * 70f * 1 / 4 +
                    -(-1 * 100f + 1f) * 70f - (10 * 100f + 1f) * 70f * 1 / 10
        ),
    )

    private fun tradeUsd(
        symbol: String,
        date: String,
        quantity: Int,
    ): TradeOrder {
        return TradeOrder(
            symbol,
            "USD",
            LocalDate.parse(date),
            quantity,
            100f,
            -1f
        )
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun getSellDetails(
        @Suppress("UNUSED_PARAMETER") caseDescription: String,
        trades: List<TradeOrder>, expected: List<SellOperationDetails>
    ) {
        val actual = PnlCalculator(currencyRatesProvider).getSellDetails(trades)

        actual shouldBe expected
    }

    @Suppress("unused")
    private fun getSellDetails() = listOf<Arguments>(
        Arguments.of(
            "simple buy and sell",
            listOf(
                tradeUsd("AAPL", "2020-12-05", 5),
                tradeUsd("AAPL", "2020-12-10", -5),
            ),
            listOf(
                sellOperationDetails(
                    "AAPL", "2020-12-10", -5,
                    purchaseOperationDetails("AAPL", "2020-12-05", 5, 5)
                )
            )
        ),
        Arguments.of(
            "partial sell",
            listOf(
                tradeUsd("AAPL", "2020-12-05", 5),
                tradeUsd("AAPL", "2020-12-06", 4),
                tradeUsd("AAPL", "2020-12-10", -6),
            ),
            listOf(
                sellOperationDetails(
                    "AAPL", "2020-12-10", -6,
                    purchaseOperationDetails("AAPL", "2020-12-05", 5, 5),
                    purchaseOperationDetails("AAPL", "2020-12-06", 4, 1),
                )
            )
        ),
        Arguments.of(
            "two symbols",
            listOf(
                tradeUsd("AAPL", "2020-12-05", 5),
                tradeUsd("AAPL", "2020-12-06", 4),
                tradeUsd("AAPL", "2020-12-10", -6),
                tradeUsd("AMZN", "2020-12-05", 10),
                tradeUsd("AMZN", "2020-12-11", -1),
            ),
            listOf(
                sellOperationDetails(
                    "AAPL", "2020-12-10", -6,
                    purchaseOperationDetails("AAPL", "2020-12-05", 5, 5),
                    purchaseOperationDetails("AAPL", "2020-12-06", 4, 1),
                ),
                sellOperationDetails(
                    "AMZN", "2020-12-11", -1,
                    purchaseOperationDetails("AMZN", "2020-12-05", 10, 1)
                )
            )
        ),
    )

    private fun sellOperationDetails(
        symbol: String,
        date: String,
        quantity: Int,
        vararg purchaseOperationDetails: PurchaseOperationDetails
    ): SellOperationDetails = SellOperationDetails(
        tradeUsd(symbol, date, quantity),
        70f,
        purchaseOperationDetails.toList()
    )

    private fun purchaseOperationDetails(
        symbol: String,
        date: String,
        quantity: Int,
        quantitySold: Int
    ): PurchaseOperationDetails =
        PurchaseOperationDetails(
            tradeUsd(symbol, date, quantity),
            70f,
            quantitySold
        )
}