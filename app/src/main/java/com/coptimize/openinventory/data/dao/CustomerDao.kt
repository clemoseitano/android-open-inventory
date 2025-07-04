package com.coptimize.openinventory.data.dao

import androidx.room.*
import com.coptimize.openinventory.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long
}