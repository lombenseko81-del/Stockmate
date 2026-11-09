package com.zut.stockmate

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // Retrieve all inventory items
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryEntity>>

    // Retrieve one item using its ID
    @Query("SELECT * FROM inventory WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): InventoryEntity?

    // Insert a new inventory item
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryEntity)

    // Update an existing inventory item
    @Update
    suspend fun updateItem(item: InventoryEntity)

    // Delete an inventory item
    @Delete
    suspend fun deleteItem(item: InventoryEntity)

    // Count inventory records
    @Query("SELECT COUNT(*) FROM inventory")
    suspend fun getItemCount(): Int
}
