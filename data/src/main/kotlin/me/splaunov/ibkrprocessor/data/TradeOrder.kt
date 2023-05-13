package me.splaunov.ibkrprocessor.data

import java.time.Instant

data class TradeOrder(
    var symbol: String,
    val currency: String,
    val date: Instant,
    var quantity: Float,
    var price: Float,
    val commission: Float,
) {

    /**
     * Calculates the fraction of proceeds with the given quantity
     * and subtracts commissions from the result.
     *
     * @param proceedsQuantity The shares quantity for proceeds calculation.
     * @return Calculated proceeds value in base currency.
     */
    fun getProceedsBaseCurrency(proceedsQuantity: Float = quantity): Float {
        return -(quantity * price - commission) * proceedsQuantity / quantity
    }

    fun isSelling(): Boolean = quantity < 0
}