package com.spendsense.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spendsense.app.data.db.dao.*
import com.spendsense.app.data.db.entity.*

/**
 * Main Room database for SpendSense.
 * Uses SQLCipher encryption (configured via DatabaseModule).
 */
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        MerchantMappingEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun merchantMappingDao(): MerchantMappingDao

    companion object {
        const val DATABASE_NAME = "spendsense_db"
    }
}
