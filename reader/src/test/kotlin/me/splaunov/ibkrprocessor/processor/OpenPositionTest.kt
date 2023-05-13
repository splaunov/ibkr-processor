package me.splaunov.ibkrprocessor.processor

import io.kotest.assertions.throwables.shouldThrowAnyUnit
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import me.splaunov.ibkrprocessor.data.PurchaseDetails
import me.splaunov.ibkrprocessor.data.SellingDetails
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.reader.Acquisition
import me.splaunov.ibkrprocessor.reader.StockSplit
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant
import kotlin.reflect.KClass

class OpenPositionTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun `processTradeOrder should fail with exception`(
        caseDescription: String,
        tradeOrders: List<TradeOrder>,
        exceptionClass: KClass<Exception>,
    ) {
        val openPosition = OpenPosition("TSLA", "USD")

        val actual = shouldThrowAnyUnit {
            tradeOrders.forEach {
                openPosition.processPurchase(it)
            }
        }
        actual::class shouldBe exceptionClass
    }

    private fun `processTradeOrder should fail with exception`() = listOf<Arguments>(
        Arguments.of(
            "should fail if symbol is wrong",
            listOf(
                TradeOrder("CAT", "USD", Instant.now(), 10F, 100F, 0F),
            ),
            IllegalArgumentException::class
        ),
        Arguments.of(
            "should fail if currency is wrong",
            listOf(
                TradeOrder("TSLA", "EUR", Instant.now(), 10F, 100F, 0F),
            ),
            IllegalArgumentException::class
        ),
        Arguments.of(
            "should fail if trade order date is later than last processed order date",
            listOf(
                TradeOrder("TSLA", "USD", Instant.now(), 10F, 2F, 0F),
                TradeOrder("TSLA", "USD", Instant.now().minusSeconds(600), 10F, 2F, 0F),
            ),
            IllegalStateException::class
        ),
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun `processTradeOrder should succeed`(
        caseDescription: String,
        tradeOrders: List<TradeOrder>,
        expectedPurchases: List<PurchaseDetails>,
        expectedSellingDetails: List<SellingDetails>,
    ) {
        val openPosition = OpenPosition("TSLA", "USD")
        val expectedSellingDetailsIterator = expectedSellingDetails.iterator()

        tradeOrders.forEach {
            if (it.isSelling()) {
                openPosition.processSelling(it) shouldBe expectedSellingDetailsIterator.next()
            } else {
                openPosition.processPurchase(it)
            }
        }

        openPosition.lastProcessedOperationDate shouldBe tradeOrders.last().date
        openPosition.purchases shouldContainExactly expectedPurchases
    }

    private fun `processTradeOrder should succeed`() = listOf<Arguments>(
        Arguments.of(
            "single purchase",
            listOf(
                tradeOrder(0, 10F),
            ),
            listOf(
                PurchaseDetails(tradeOrder(0, 10F), 0F),
            ),
            listOf<SellingDetails>(),
        ),
        Arguments.of(
            "two purchases",
            listOf(
                tradeOrder(0, 10F),
                tradeOrder(10, 2F),
            ),
            listOf(
                PurchaseDetails(tradeOrder(0, 10F), 0F),
                PurchaseDetails(tradeOrder(10, 2F), 0F),
            ),
            listOf<SellingDetails>(),
        ),
        Arguments.of(
            "purchase more and sell less",
            listOf(
                tradeOrder(0, 10F),
                tradeOrder(10, -2F),
            ),
            listOf(
                PurchaseDetails(tradeOrder(0, 10F), 2F),
            ),
            listOf(
                SellingDetails(
                    tradeOrder(10, -2F),
                    listOf(
                        PurchaseDetails(tradeOrder(0, 10F), 2F),
                    ),
                ),
            ),
        ),
        Arguments.of(
            "purchase and sell equal",
            listOf(
                tradeOrder(0, 10F),
                tradeOrder(10, -10F),
            ),
            listOf<PurchaseDetails>(),
            listOf(
                SellingDetails(
                    tradeOrder(10, -10F),
                    listOf(
                        PurchaseDetails(tradeOrder(0, 10F), 10F),
                    ),
                ),
            ),
        ),
        Arguments.of(
            "two small purchases and one bigger sell",
            listOf(
                tradeOrder(0, 2F),
                tradeOrder(10, 2F),
                tradeOrder(20, -3F),
            ),
            listOf(
                PurchaseDetails(tradeOrder(10, 2F), 1F),
            ),
            listOf(
                SellingDetails(
                    tradeOrder(20, -3F),
                    listOf(
                        PurchaseDetails(tradeOrder(0, 2F), 2F),
                        PurchaseDetails(tradeOrder(10, 2F), 1F),
                    ),
                ),
            ),
        ),
        Arguments.of(
            "one big purchase and two smaller sells",
            listOf(
                tradeOrder(0, 10F),
                tradeOrder(10, -2F),
                tradeOrder(20, -3F),
            ),
            listOf(
                PurchaseDetails(tradeOrder(0, 10F), 5F),
            ),
            listOf(
                SellingDetails(
                    tradeOrder(10, -2F),
                    listOf(
                        PurchaseDetails(tradeOrder(0, 10F), 2F),
                    ),
                ),
                SellingDetails(
                    tradeOrder(20, -3F),
                    listOf(
                        PurchaseDetails(tradeOrder(0, 10F), 3F),
                    ),
                ),
            ),
        ),
    )

    @Test
    fun `processSplit should not change selling details`() {
        val openPosition = OpenPosition("TSLA", "USD")
        openPosition.processPurchase(tradeOrder(0, 10F, price = 2F))
        val sellingDetails = openPosition.processSelling(tradeOrder(10, -5F, price = 3F))

        openPosition.processSplit(stockSplit(20, 2))

        openPosition.purchases shouldContainExactly listOf(
            PurchaseDetails(tradeOrder(0, 20F, price = 1F), 10F),
        )
        sellingDetails.purchases shouldContainExactly listOf(
            PurchaseDetails(tradeOrder(0, 10F, price = 2F), 5F),
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun `processSplit should succeed`(
        caseDescription: String,
        tradeOrders: List<TradeOrder>,
        split: StockSplit,
        expectedPurchases: List<PurchaseDetails>,
    ) {
        val openPosition = OpenPosition("TSLA", "USD")
        tradeOrders.forEach { openPosition.processPurchase(it) }

        openPosition.processSplit(split)

        openPosition.lastProcessedOperationDate shouldBe split.date
        openPosition.purchases shouldContainExactly expectedPurchases
    }

    private fun `processSplit should succeed`() = listOf<Arguments>(
        Arguments.of(
            "two purchases and one split",
            listOf(
                tradeOrder(0, 10F, price = 2F),
                tradeOrder(10, 2F, price = 2F),
            ),
            stockSplit(20, 2),
            listOf(
                PurchaseDetails(tradeOrder(0, 20F, price = 1F), 0F),
                PurchaseDetails(tradeOrder(10, 4F, price = 1F), 0F),
            ),
        )
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource
    fun `processAcquisition should succeed`(
        caseDescription: String,
        tradeOrders: List<TradeOrder>,
        acquisition: Acquisition,
        ecpectedSymbol: String,
        expectedPurchases: List<PurchaseDetails>,
    ) {
        val openPosition = OpenPosition("ALXN", "USD")
        tradeOrders.forEach {
            if (it.isSelling()) {
                openPosition.processSelling(it)
            } else {
                openPosition.processPurchase(it)
            }
        }

        openPosition.processAcquisition(acquisition)

        openPosition.lastProcessedOperationDate shouldBe acquisition.date
        openPosition.symbol shouldBe "AZN"
        openPosition.purchases shouldContainExactly expectedPurchases
    }

    private fun `processAcquisition should succeed`() = listOf<Arguments>(
        Arguments.of(
            "one purchases and one acquisition",
            listOf(
                tradeOrder(0, 5F, "ALXN", 100F),
                tradeOrder(10, -1F, "ALXN", 120F),
            ),
            acquisition(20, "ALXN", -4F, 240F, "AZN", 8.4972F, 0F),
            "AZN",
            listOf(
                PurchaseDetails(tradeOrder(0, 10.6215F, "AZN", 40F*4/8.4972F), 2.1243F),
            ),
        )
    )

    private fun tradeOrder(orderTimeInSeconds: Int, quantity: Float, symbol: String = "TSLA", price: Float = 2F) =
        TradeOrder(
            symbol,
            "USD",
            Instant.parse("2020-08-20T00:00:00Z").plusSeconds(orderTimeInSeconds.toLong()),
            quantity.toFloat(),
            price,
            0F
        )

    private fun stockSplit(splitTimeInSeconds: Int, multiplier: Int) = StockSplit(
        "TSLA",
        Instant.parse("2020-08-20T00:00:00Z").plusSeconds(splitTimeInSeconds.toLong()),
        multiplier
    )

    private fun acquisition(
        actionTimeInSeconds: Int,
        firstSymbol: String,
        firstQuantity: Float,
        firstProceeds: Float,
        secondSymbol: String,
        secondQuantity: Float,
        secondProceeds: Float,
    ) = Acquisition(
        Instant.parse("2020-08-20T00:00:00Z").plusSeconds(actionTimeInSeconds.toLong()),
        firstSymbol,
        firstQuantity,
        firstProceeds,
        secondSymbol,
        secondQuantity,
        secondProceeds,
    )
}