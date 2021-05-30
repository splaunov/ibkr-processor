package me.splaunov.ibkrprocessor.data

import java.time.LocalDate

data class TradeOrder(
    val symbol: String,
    val currency: String,
    val date: LocalDate,
    val quantity: Int,
    val price: Float,
    val commission: Float
) {

    /**
     * Calculates the fraction of proceeds with the given quantity.
     * Subtracts commissions from the result.
     *
     * @param proceedsQuantity The shares quantity for proceeds calculation.
     * @return Calculated proceeds value in base currency.
     */
    fun getProceedsBaseCurrency(proceedsQuantity: Int = quantity): Float {
        return -(quantity * price - commission) * proceedsQuantity / quantity
    }

    fun isSale(): Boolean = quantity < 0
}