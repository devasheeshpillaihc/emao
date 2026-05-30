package com.spendsense.app.domain.repository

import com.spendsense.app.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for transaction operations.
 * Defines the contract between data and domain layers.
 */
interface TransactionRepository {

    // ── Observe ──
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun getTransactionsByCategory(category: Category): Flow<List<Transaction>>

    // ── Aggregations ──
    fun getTotalSpending(startDate: Long, endDate: Long): Flow<Double>
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double>
    fun getCategorySpending(startDate: Long, endDate: Long): Flow<List<CategorySpending>>
    fun getTopMerchants(startDate: Long, endDate: Long, limit: Int = 10): Flow<List<MerchantSpending>>
    fun getMonthlyTrends(startDate: Long, endDate: Long): Flow<List<MonthlyTrend>>

    // ── Search & Filter ──
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun getFilteredTransactions(filter: TransactionFilter): Flow<List<Transaction>>

    // ── CRUD ──
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun insertParsedTransaction(parsed: ParsedTransaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)

    // ── Duplicate Check ──
    suspend fun existsByFingerprint(fingerprint: String): Boolean

    // ── Count ──
    fun getTransactionCount(): Flow<Int>
}

/**
 * Repository interface for budget operations.
 */
interface BudgetRepository {

    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>
    fun getOverallBudget(month: Int, year: Int): Flow<Budget?>

    suspend fun insertBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(id: Long)
}

/**
 * Repository interface for recurring transactions.
 */
interface RecurringTransactionRepository {

    fun getActiveRecurring(): Flow<List<RecurringTransaction>>
    fun getAllRecurring(): Flow<List<RecurringTransaction>>

    suspend fun insertRecurring(recurring: RecurringTransaction): Long
    suspend fun updateRecurring(recurring: RecurringTransaction)
    suspend fun deleteRecurring(recurring: RecurringTransaction)
    suspend fun getDueRecurring(): List<RecurringTransaction>
}
