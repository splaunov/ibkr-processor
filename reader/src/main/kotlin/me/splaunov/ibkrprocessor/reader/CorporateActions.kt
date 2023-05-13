package me.splaunov.ibkrprocessor.reader

import java.time.Instant

data class CorporateAction(
    val date: Instant,
    val details: Any,
) {
    constructor(split: StockSplit) : this(split.date, split)
    constructor(acquisition: Acquisition) : this(acquisition.date, acquisition)
    constructor(isinChange: IsinChange) : this(isinChange.date, isinChange)
}

data class StockSplit(
    val symbol: String,
    val date: Instant,
    val multiplier: Int,
)

data class Acquisition(
    val date: Instant,
    val firstSymbol: String,
    val firstQuantity: Float,
    val firstProceeds: Float,
    val secondSymbol: String,
    val secondQuantity: Float,
    val secondProceeds: Float,
)

data class IsinChange(
    val date: Instant,
    val oldSymbol: String,
    val oldQuantity: Float,
    val newSymbol: String,
    val newQuantity: Float,
)
