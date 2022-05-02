package me.splaunov.ibkrprocessor.reader

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import me.splaunov.ibkrprocessor.data.TradeOrder
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import jakarta.inject.Inject

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

        actual shouldHaveSize 76
        actual shouldContain TradeOrder(
            "AAPL", "USD",
            LocalDate.parse("2020-08-21"), 9 * 4, 490.54f / 4, -1.0f//, 73.7711f
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
            StockSplit("AAPL", LocalDate.parse("2020-08-28"), 4),
        )
    }
}