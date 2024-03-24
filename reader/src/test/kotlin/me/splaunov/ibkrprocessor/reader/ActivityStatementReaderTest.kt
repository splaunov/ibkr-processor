package me.splaunov.ibkrprocessor.reader

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import me.splaunov.ibkrprocessor.data.TradeOrder
import java.io.File
import java.time.Instant

@MicronautTest
class ActivityStatementReaderTest(
    private val reader: ActivityStatementReader,
) : StringSpec({

    val statementsDir = File(
        ActivityStatementReader::class.java.classLoader.getResource(
            "statements"
        )!!.file
    )

    "read trades" {

        val actual = reader.readTrades(statementsDir)

        actual shouldHaveSize 77
        actual shouldContain TradeOrder(
            "AAPL", "USD",
            Instant.parse("2020-08-21T10:54:34.00Z"), 9F, 490.54f, -1.0f//, 73.7711f
        )
    }

    "read corporate actions" {

        val actual = reader.readCorporateActions(statementsDir)

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

    "read instrument information" {

        val actual = reader.readInstrumentInformation(statementsDir)

        actual.size shouldBe 13
    }
})