package com.spendsense.app.data.db.dao

import androidx.room.*
import com.spendsense.app.data.db.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringTransactionEntity): Long

    @Update
    suspend fun update(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurring: RecurringTransactionEntity)

    @Query("SELECT * FROM recurring_transactions WHERE is_active = 1 ORDER BY next_due_date ASC")
    fun getActiveRecurring(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions ORDER BY created_at DESC")
    fun getAllRecurring(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE next_due_date <= :now AND is_active = 1")
    suspend fun getDueRecurring(now: Long): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?
}
