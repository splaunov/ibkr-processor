package me.splaunov.ibkrprocessor.data

data class PurchaseDetails(
    val purchaseOrder: TradeOrder,
    var quantitySold: Float
)