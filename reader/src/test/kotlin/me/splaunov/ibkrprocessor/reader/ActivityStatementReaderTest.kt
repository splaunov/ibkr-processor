package me.splaunov.ibkrprocessor.reader

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import me.splaunov.ibkrprocessor.data.TradeOrder
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant

@MicronautTest
class ActivityStatementReaderTest {

    @Inject
    lateinit var reader: ActivityStatementReader

    @Test
    fun readTrades() {

        val actual = reader.readTrades(
            File(
                ActivityStatementReader::class.java.classLoader.getResource(
                    "statements"
                )!!.file
            )
        )

        actual shouldHaveSize 77
        actual shouldContain TradeOrder(
            "AAPL", "USD",
            Instant.parse("2020-08-21T10:54:34.00Z"), 9F, 490.54f, -1.0f//, 73.7711f
        )
    }

    @Test
    fun readCorporateActions() {
        val actual = reader.readCorporateActions(
            File(
                ActivityStatementReader::class.java.classLoader.getResource(
                    "statements"
                )!!.file
            )
        )

        actual shouldContainExactlyInAnyOrder listOf(
            CorporateAction(
                StockSplit(
                    "AAPL",
                    Instant.parse("2020-08-28T20:25:00Z"),
                    4
                )
            ),
            CorporateAction(
                Acquisition(
                    Instant.parse("2021-07-20T20:25:00Z"),
                    "ALXN",
                    -4F,
                    240F,
                    "AZN",
                    8.4972F,
                    0F
                )
            ),
            CorporateAction(
                IsinChange(
                    Instant.parse("2022-04-08T20:25:00Z"),
                    "TODO.old",
                    0F,
                    "TODO.new",
                    0F
                )
            ),
            CorporateAction(
                Acquisition(
                    Instant.parse("2022-05-26T20:25:00Z"),
                    "QDEL",
                    10F,
                    0F,
                    "QDEL.OLD",
                    -10F,
                    0F
                )
            ),
        )
    }
}