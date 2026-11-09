package com.zut.stockmate

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        InventoryEntity::class,
        SaleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StockMateDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao

    abstract fun saleDao(): SaleDao

    companion object {

        @Volatile
        private var INSTANCE: StockMateDatabase? = null

        fun getDatabase(context: Context): StockMateDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockMateDatabase::class.java,
                    "stockmate_database"
                ).build()

                INSTANCE = instance

                instance
            }
        }
    }
}
