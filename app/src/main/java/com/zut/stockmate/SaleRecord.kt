package com.zut.stockmate

data class SaleRecord(
    val id: Int,
    val itemId: Int,
    val itemName: String,
    val quantitySold: Int,
    val unitPrice: Double,
    val total: Double,
    val dateTimeMillis: Long
)