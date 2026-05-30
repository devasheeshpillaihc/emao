package com.spendsense.app.data.repository

import com.spendsense.app.data.db.EntityMappers
import com.spendsense.app.data.db.dao.TransactionDao
import com.spendsense.app.data.db.dao.MerchantMappingDao
import com.spendsense.app.data.db.entity.MerchantMappingEntity
import com.spendsense.app.data.db.toDomain
import com.spendsense.app.data.db.toEntity
import com.spendsense.app.data.parser.CategoryPredictor
import com.spendsense.app.data.parser.DuplicateDetector
import com.spendsense.app.data.parser.MerchantNormalizer
import com.spendsense.app.domain.model.*
import com.spendsense.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TransactionRepository.
 * Handles the full pipeline: normalize → categorize → deduplicate → store.
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val merchantMappingDao: MerchantMappingDao,
    private val merchantNormalizer: MerchantNormalizer,
    private val categoryPredictor: CategoryPredictor,
    private val duplicateDetector: DuplicateDetector
) : TransactionRepository {

    // ── Observe ──

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByCategory(category: Category): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ── Aggregations ──

    override fun getTotalSpending(startDate: Long, endDate: Long): Flow<Double> {
        return transactionDao.getTotalSpending(startDate, endDate)
    }

    override fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double> {
        return transactionDao.getTotalIncome(startDate, endDate)
    }

    override fun getCategorySpending(startDate: Long, endDate: Long): Flow<List<CategorySpending>> {
        return transactionDao.getCategorySpending(startDate, endDate).map { tuples ->
            val total = tuples.sumOf { it.total }
            tuples.map { it.toDomain(total) }
        }
    }

    override fun getTopMerchants(startDate: Long, endDate: Long, limit: Int): Flow<List<MerchantSpending>> {
        return transactionDao.getTopMerchants(startDate, endDate, limit).map { tuples ->
            tuples.map { it.toDomain() }
        }
    }

    override fun getMonthlyTrends(startDate: Long, endDate: Long): Flow<List<MonthlyTrend>> {
        return transactionDao.getMonthlyTrends(startDate, endDate).map { tuples ->
            tuples.map { it.toDomain() }
        }
    }

    // ── Search & Filter ──

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFilteredTransactions(filter: TransactionFilter): Flow<List<Transaction>> {
        return transactionDao.getFilteredTransactions(
            startDate = filter.startDate,
            endDate = filter.endDate,
            category = filter.categories.firstOrNull()?.name,
            minAmount = filter.minAmount,
            maxAmount = filter.maxAmount,
            transactionType = filter.transactionTypes.firstOrNull()?.name,
            direction = filter.direction?.name,
            merchant = filter.merchants.firstOrNull(),
            query = filter.searchQuery.ifBlank { null }
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ── CRUD ──

    override suspend fun insertTransaction(transaction: Transaction): Long {
        // Generate fingerprint
        val fingerprint = duplicateDetector.generateFingerprint(
            amount = transaction.amount,
            merchant = transaction.normalizedMerchant.ifBlank { transaction.merchant },
            timestamp = transaction.dateTime,
            referenceNumber = transaction.referenceNumber
        )

        // Check for duplicates
        if (transactionDao.existsByFingerprint(fingerprint)) {
            return -1 // Duplicate
        }

        val entity = transaction.copy(
            fingerprint = fingerprint,
            updatedAt = System.currentTimeMillis()
        ).toEntity()

        return transactionDao.insert(entity)
    }

    override suspend fun insertParsedTransaction(parsed: ParsedTransaction): Long {
        // Normalize merchant
        val normalizedMerchant = merchantNormalizer.normalize(parsed.merchant)

        // Predict category
        val category = categoryPredictor.predict(normalizedMerchant, parsed.sourceRaw)

        // Generate fingerprint
        val fingerprint = duplicateDetector.generateFingerprint(
            amount = parsed.amount,
            merchant = normalizedMerchant,
            timestamp = parsed.dateTime,
            referenceNumber = parsed.referenceNumber
        )

        // Check for duplicates
        if (transactionDao.existsByFingerprint(fingerprint)) {
            return -1 // Duplicate
        }

        // Build full transaction
        val transaction = Transaction(
            amount = parsed.amount,
            currency = parsed.currency,
            merchant = parsed.merchant,
            normalizedMerchant = normalizedMerchant,
            category = category,
            transactionType = parsed.transactionType,
            direction = parsed.direction,
            dateTime = parsed.dateTime,
            referenceNumber = parsed.referenceNumber,
            source = parsed.source,
            sourceRaw = parsed.sourceRaw,
            accountInfo = parsed.accountInfo,
            fingerprint = fingerprint,
            status = parsed.status
        )

        val id = transactionDao.insert(transaction.toEntity())

        // Update merchant mapping for future lookups
        if (normalizedMerchant.isNotBlank()) {
            val existingMapping = merchantMappingDao.getByRawName(parsed.merchant)
            if (existingMapping != null) {
                merchantMappingDao.incrementFrequency(parsed.merchant)
            } else {
                merchantMappingDao.upsert(
                    MerchantMappingEntity(
                        rawName = parsed.merchant,
                        normalizedName = normalizedMerchant,
                        category = category.name,
                        frequency = 1
                    )
                )
            }
        }

        return id
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(
            transaction.copy(
                isEdited = true,
                updatedAt = System.currentTimeMillis()
            ).toEntity()
        )
    }

    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    // ── Duplicate Check ──

    override suspend fun existsByFingerprint(fingerprint: String): Boolean {
        return transactionDao.existsByFingerprint(fingerprint)
    }

    // ── Count ──

    override fun getTransactionCount(): Flow<Int> {
        return transactionDao.getTransactionCount()
    }
}
