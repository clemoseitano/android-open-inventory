package com.coptimize.openinventory.data.dao

import androidx.room.*
import com.coptimize.openinventory.data.model.Sale
import com.coptimize.openinventory.data.model.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(saleItems: List<SaleItem>)

    // Example of a more complex query for reports
    @Query("""
        SELECT * FROM sales
        WHERE DATE(created_at) BETWEEN :startDate AND :endDate
        ORDER BY created_at DESC
    """)
    fun getSalesBetweenDates(startDate: String, endDate: String): Flow<List<Sale>>
}