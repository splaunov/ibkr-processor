package me.splaunov.ibkrprocessor.data

data class SellingDetails(
    val sellOrder: TradeOrder,
    val purchases: List<PurchaseDetails>
)

