package com.zut.stockmate

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    // Retrieve all sales transactions
    @Query("SELECT * FROM sales ORDER BY dateTimeMillis DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    // Insert a new sales transaction
    @Insert
    suspend fun insertSale(sale: SaleEntity)

    // Retrieve sales within a specific date range
    @Query("""
        SELECT * FROM sales
        WHERE dateTimeMillis >= :startTime
        AND dateTimeMillis < :endTime
        ORDER BY dateTimeMillis DESC
    """)
    fun getSalesBetween(
        startTime: Long,
        endTime: Long
    ): Flow<List<SaleEntity>>

    // Calculate total sales revenue
    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sales")
    fun getTotalSales(): Flow<Double>
}
