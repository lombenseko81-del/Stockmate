package com.zut.stockmate

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val quantity: Int,

    val price: Double,

    val lowStockLimit: Int
)
