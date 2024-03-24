package me.splaunov.ibkrprocessor.reader

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import jakarta.inject.Singleton
import me.splaunov.ibkrprocessor.data.Dividend
import me.splaunov.ibkrprocessor.data.InstrumentInformation
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.reader.TradeOrderFields.COMMISSION
import me.splaunov.ibkrprocessor.reader.TradeOrderFields.CURRENCY
import me.splaunov.ibkrprocessor.reader.TradeOrderFields.DATE
import me.splaunov.ibkrprocessor.reader.TradeOrderFields.PRICE
import me.splaunov.ibkrprocessor.reader.TradeOrderFields.QUANTITY
import me.splaunov.ibkrprocessor.reader.TradeOrderFields.SYMBOL
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Reads blocks of information from the IBKR Activity statement.
 */
@Singleton
class ActivityStatementReader {

    /**
     * Reads trade orders' data.
     *
     * @param dir Directory from which activity statement files should be read.
     * @return List of trade orders.
     */
    fun readTrades(dir: File): List<TradeOrder> {
        val trades = mutableListOf<TradeOrder>()

        dir.listFiles { f -> f.extension == "csv" }?.forEach { file ->
            csvReader().open(file) {
                var record = readNext()
                while (record != null) {
                    if (record[0] == "Trades" && record[1] == "Data" && record[2] == "Order" && record[3] == "Stocks") {
                        trades.add(
                            TradeOrder(
                                record[SYMBOL],
                                record[CURRENCY],
                                record[DATE].toInstant(),
                                record[QUANTITY].toFloat(),
                                record[PRICE].toFloat(),
                                record[COMMISSION].toFloat()
                            )
                        )
                    }
                    record = readNext()
                }
            }
        }

        return trades
    }

    /**
     * Reads info regarding stocks splits.
     */
    fun readCorporateActions(dir: File): List<CorporateAction> {
        val actions = mutableListOf<CorporateAction>()
        dir.listFiles { f -> f.extension == "csv" }?.forEach { file ->
            csvReader().open(file) {
                var record = readNext()
                while (record != null) {
                    if (record[0] == "Corporate Actions" && record[1] == "Data" && record[2] == "Stocks"
                        && !record[CorporateActionFields.DESCRIPTION].contains("US82452T1079") //skip SFT for now
                    //TODO Process SFT action when it is sold
                    ) {
                        val date = record[CorporateActionFields.DATE].toInstant()
                        val description = record[CorporateActionFields.DESCRIPTION]
                        when {
                            description.contains("Split") -> actions.add(CorporateAction(readSplit(record, date)))
                            description.contains("Acquisition") ->
                                actions.add(CorporateAction(readAcquisition(record, readNext(), date)))

                            description.contains("CUSIP/ISIN Change") ->
                                actions.add(CorporateAction(readIsinChange(record, readNext(), date)))

                            else -> error("Unknown corporate event: $record")
                        }
                    }
                    record = readNext()
                }
            }
        }
        return actions
    }

    fun readInstrumentInformation(dir: File): Map<String, InstrumentInformation> {
        val result = mutableMapOf<String, InstrumentInformation>()
        dir.listFiles { f -> f.extension == "csv" }?.forEach { file ->
            csvReader().open(file) {
                generateSequence { readNext() }.forEach { record ->
                    if (record[0] == "Financial Instrument Information"
                        && record[1] == "Data"
                        && record[2] == "Stocks"
                    ) {
                        result[record[InstrumentInformationFields.SYMBOL]] = InstrumentInformation(
                            record[InstrumentInformationFields.SYMBOL],
                            record[InstrumentInformationFields.SECURITY_ID],
                        )
                    }
                }
            }
        }
        return result
    }

    private fun readAcquisition(firstRecord: List<String>, secondRecord: List<String>?, date: Instant): Acquisition {
        checkNotNull(secondRecord) { "Missed second record of acquisition event" }
        val regex = """^(.+)\(.+\) .+\(Acquisition\) .+\((.+), .+, .+\)$""".toRegex()
        val firstMatch = regex.matchEntire(firstRecord[CorporateActionFields.DESCRIPTION])
            ?: error("Unknown corporate action: ${firstRecord[CorporateActionFields.DESCRIPTION]}")
        val secondMatch = regex.matchEntire(secondRecord[CorporateActionFields.DESCRIPTION])
            ?: error("Unknown corporate action: ${secondRecord[CorporateActionFields.DESCRIPTION]}")
        check(secondMatch.groupValues[1] != secondMatch.groupValues[2]) {
            "Error parsing acquisition description: ${secondRecord[CorporateActionFields.DESCRIPTION]}"
        }

        return Acquisition(
            date,
            firstMatch.groupValues[2].split(".", limit = 1).last(), //e.g. QDEL.OLD
            firstRecord[CorporateActionFields.QUANTITY].toFloat(),
            firstRecord[CorporateActionFields.PROCEEDS].toFloat(),
            secondMatch.groupValues[2].split(".", limit = 1).last(),
            secondRecord[CorporateActionFields.QUANTITY].toFloat(),
            secondRecord[CorporateActionFields.PROCEEDS].toFloat(),
        )
    }

    private fun readIsinChange(firstRecord: List<String>, secondRecord: List<String>?, date: Instant): IsinChange {
        checkNotNull(secondRecord) { "Missed second record of ISIN change event" }
        //TODO Implement parsing of ISIN change info
        return IsinChange(date, "TODO.old", 0F, "TODO.new", 0F)
    }

    private fun readSplit(record: List<String>, date: Instant): StockSplit {
        val regex = """^(\D+)\(\w+\) Split (\d+) for (\d+) \(\1, \D+, \w+\)""".toRegex()
        val match = regex.matchEntire(record[CorporateActionFields.DESCRIPTION])
            ?: error("Unknown corporate event: $record")
        if (match.groupValues[3].toInt() != 1)
            error("Unknown corporate event: $record")
        return StockSplit(match.groupValues[1], date, match.groupValues[2].toInt())
    }

}

private fun String.toInstant(): Instant =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern("uuuu-M-d, HH:mm:ss")).atZone(ZoneId.of("UTC")).toInstant()

fun readDividends(file: File): List<Dividend> {
    TODO("Implement dividends reader")
}

enum class TradeOrderFields(val index: Int) {
    CURRENCY(4),
    SYMBOL(5),
    DATE(6),
    QUANTITY(7),
    PRICE(8),
    COMMISSION(11)
}

private operator fun List<String>.get(field: TradeOrderFields): String {
    return this[field.index]
}

enum class CorporateActionFields(val index: Int) {
    DATE(5),
    DESCRIPTION(6),
    QUANTITY(7),
    PROCEEDS(8),
}

private operator fun List<String>.get(field: CorporateActionFields): String {
    return this[field.index]
}

enum class InstrumentInformationFields(val index: Int) {
    SYMBOL(3),
    SECURITY_ID(6),
}

private operator fun List<String>.get(field: InstrumentInformationFields): String {
    return this[field.index]
}
