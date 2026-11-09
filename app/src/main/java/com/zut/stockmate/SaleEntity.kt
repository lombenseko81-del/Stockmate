package com.zut.stockmate

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val itemId: Int,

    val itemName: String,

    val quantitySold: Int,

    val unitPrice: Double,

    val total: Double,

    val dateTimeMillis: Long
)
