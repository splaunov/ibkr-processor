package me.splaunov.ibkrprocessor.data

data class SellOperationDetails(
    val sellOrder: TradeOrder,
    val currencyRate: Float,
    val purchases: List<PurchaseOperationDetails>
)

data class PurchaseOperationDetails(
    val purchaseOrder: TradeOrder,
    val currencyRate: Float,
    val quantitySold: Int
)