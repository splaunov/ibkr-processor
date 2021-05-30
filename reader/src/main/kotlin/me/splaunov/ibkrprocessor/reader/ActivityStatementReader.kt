package me.splaunov.ibkrprocessor.reader

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import me.splaunov.ibkrprocessor.data.Dividend
import me.splaunov.ibkrprocessor.data.TradeOrder
import me.splaunov.ibkrprocessor.reader.TradeOrderFields.*
import java.io.File
import java.time.LocalDate
import javax.inject.Singleton

/**
 * Reads blocks of information from the IBKR Activity statement.
 */
@Singleton
class ActivityStatementReader {

    /**
     * Reads trade orders' data.
     * Quantity and prices are adjusted according to stock splits if applicable.
     *
     * @param file Activity statement file to read.
     * @return List of trade orders adjusted according to splits.
     */
    fun readTrades(file: File): List<TradeOrder> {
        val stockSplits = readSplits(file)
        val trades = mutableListOf<TradeOrder>()
        csvReader().open(file) {
            var record = readNext()
            while (record != null) {
                if (record[0] == "Trades" && record[1] == "Data" && record[2] == "Order" && record[3] == "Stocks") {
                    val date = LocalDate.parse(record[DATE].take(10))
                    val splitMultiplier = getSplitMultiplier(stockSplits, record[SYMBOL], date)
                    var quantity = record[QUANTITY].toInt()
                    var price = record[PRICE].toFloat()
                    if (splitMultiplier != null) {
                        price /= splitMultiplier
                        quantity *= splitMultiplier
                    }
                    trades.add(
                        TradeOrder(
                            record[SYMBOL],
                            record[CURRENCY],
                            date,
                            quantity,
                            price,
                            record[COMMISSION].toFloat()
                        )
                    )
                }
                record = readNext()
            }
        }
        return trades
    }

    /**
     * Reads info regarding stocks splits.
     */
    fun readSplits(file: File): List<StockSplit> {
        val splits = mutableListOf<StockSplit>()
        csvReader().open(file) {
            var record = readNext()
            while (record != null) {
                if (record[0] == "Corporate Actions" && record[1] == "Data" && record[2] == "Stocks") {
                    val date = LocalDate.parse(record[CorporateActionFields.DATE].take(10))
                    val regex = """^(\D+)\(\w+\) Split (\d+) for (\d+) \(\1, \D+, \w+\)""".toRegex()
                    val match = regex.matchEntire(record[CorporateActionFields.DESCRIPTION])
                        ?: throw IllegalStateException("Unknown corporate event: $record[CorporateActionFields.DESCRIPTION]")
                    if (match.groupValues[3].toInt() != 1)
                        throw IllegalStateException("Unknown corporate event: $record[CorporateActionFields.DESCRIPTION]")
                    splits.add(
                        StockSplit(match.groupValues[1], date, match.groupValues[2].toInt())
                    )
                }
                record = readNext()
            }
        }
        return splits
    }

    private fun getSplitMultiplier(splits: List<StockSplit>, symbol: String, date: LocalDate): Int? =
        splits.firstOrNull { it.symbol == symbol && date < it.date }?.multiplier

}

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
}

private operator fun List<String>.get(field: CorporateActionFields): String {
    return this[field.index]
}
