package me.splaunov.ibkrprocessor.reader

import java.time.LocalDate

data class StockSplit(
    val symbol: String,
    val date: LocalDate,
    val multiplier: Int
)
