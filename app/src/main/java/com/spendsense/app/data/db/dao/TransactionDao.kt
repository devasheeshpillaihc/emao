package com.spendsense.app.data.db.dao

import androidx.room.*
import com.spendsense.app.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for transactions.
 * Provides reactive Flow-based queries for UI observation.
 */
@Dao
interface TransactionDao {

    // ── Insert / Update / Delete ──

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ── Basic Queries ──

    @Query("SELECT * FROM transactions ORDER BY date_time DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY date_time DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>>

    // ── Date Range Queries ──

    @Query("""
        SELECT * FROM transactions 
        WHERE date_time BETWEEN :startDate AND :endDate 
        ORDER BY date_time DESC
    """)
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    // ── Category Queries ──

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date_time DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    // ── Spending Aggregations ──

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE direction = 'DEBIT' AND status = 'SUCCESS'
        AND date_time BETWEEN :startDate AND :endDate
    """)
    fun getTotalSpending(startDate: Long, endDate: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
        WHERE direction = 'CREDIT' AND status = 'SUCCESS'
        AND date_time BETWEEN :startDate AND :endDate
    """)
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double>

    // ── Category Spending ──

    @Query("""
        SELECT category, SUM(amount) as total, COUNT(*) as count
        FROM transactions 
        WHERE direction = 'DEBIT' AND status = 'SUCCESS'
        AND date_time BETWEEN :startDate AND :endDate
        GROUP BY category
        ORDER BY total DESC
    """)
    fun getCategorySpending(startDate: Long, endDate: Long): Flow<List<CategorySpendingTuple>>

    // ── Top Merchants ──

    @Query("""
        SELECT normalized_merchant as merchant, SUM(amount) as total, COUNT(*) as count, category
        FROM transactions 
        WHERE direction = 'DEBIT' AND status = 'SUCCESS'
        AND normalized_merchant != ''
        AND date_time BETWEEN :startDate AND :endDate
        GROUP BY normalized_merchant
        ORDER BY total DESC
        LIMIT :limit
    """)
    fun getTopMerchants(startDate: Long, endDate: Long, limit: Int = 10): Flow<List<MerchantSpendingTuple>>

    // ── Search ──

    @Query("""
        SELECT * FROM transactions 
        WHERE (merchant LIKE '%' || :query || '%' 
            OR normalized_merchant LIKE '%' || :query || '%'
            OR notes LIKE '%' || :query || '%'
            OR tags LIKE '%' || :query || '%')
        ORDER BY date_time DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    // ── Filtered Search ──

    @Query("""
        SELECT * FROM transactions 
        WHERE (:startDate IS NULL OR date_time >= :startDate)
        AND (:endDate IS NULL OR date_time <= :endDate)
        AND (:category IS NULL OR category = :category)
        AND (:minAmount IS NULL OR amount >= :minAmount)
        AND (:maxAmount IS NULL OR amount <= :maxAmount)
        AND (:transactionType IS NULL OR transaction_type = :transactionType)
        AND (:direction IS NULL OR direction = :direction)
        AND (:merchant IS NULL OR normalized_merchant LIKE '%' || :merchant || '%')
        AND (:query IS NULL OR merchant LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
        ORDER BY date_time DESC
    """)
    fun getFilteredTransactions(
        startDate: Long? = null,
        endDate: Long? = null,
        category: String? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        transactionType: String? = null,
        direction: String? = null,
        merchant: String? = null,
        query: String? = null
    ): Flow<List<TransactionEntity>>

    // ── Duplicate Detection ──

    @Query("SELECT * FROM transactions WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): TransactionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE fingerprint = :fingerprint)")
    suspend fun existsByFingerprint(fingerprint: String): Boolean

    // ── Monthly Trends ──

    @Query("""
        SELECT 
            CAST(strftime('%m', date_time / 1000, 'unixepoch') AS INTEGER) as month,
            CAST(strftime('%Y', date_time / 1000, 'unixepoch') AS INTEGER) as year,
            SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE 0 END) as totalSpending,
            SUM(CASE WHEN direction = 'CREDIT' THEN amount ELSE 0 END) as totalIncome
        FROM transactions
        WHERE status = 'SUCCESS'
        AND date_time BETWEEN :startDate AND :endDate
        GROUP BY year, month
        ORDER BY year, month
    """)
    fun getMonthlyTrends(startDate: Long, endDate: Long): Flow<List<MonthlyTrendTuple>>

    // ── Count ──

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Flow<Int>
}

/**
 * Tuple for category spending aggregation query results.
 */
data class CategorySpendingTuple(
    val category: String,
    val total: Double,
    val count: Int
)

/**
 * Tuple for merchant spending aggregation query results.
 */
data class MerchantSpendingTuple(
    val merchant: String,
    val total: Double,
    val count: Int,
    val category: String
)

/**
 * Tuple for monthly trend query results.
 */
data class MonthlyTrendTuple(
    val month: Int,
    val year: Int,
    val totalSpending: Double,
    val totalIncome: Double
)
