package com.spendsense.app.domain.model

/**
 * Domain model representing a financial transaction.
 * This is the clean domain object used throughout the app.
 */
data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val currency: String = "INR",
    val merchant: String,
    val normalizedMerchant: String = "",
    val category: Category = Category.OTHER,
    val transactionType: TransactionType = TransactionType.UNKNOWN,
    val direction: TransactionDirection = TransactionDirection.DEBIT,
    val dateTime: Long = System.currentTimeMillis(),
    val referenceNumber: String? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val sourceRaw: String? = null,
    val accountInfo: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList(),
    val receiptUri: String? = null,
    val isRecurring: Boolean = false,
    val recurringId: Long? = null,
    val fingerprint: String = "",
    val status: TransactionStatus = TransactionStatus.SUCCESS,
    val isEdited: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Intermediate result from the parsing engine before full processing.
 */
data class ParsedTransaction(
    val amount: Double,
    val currency: String = "INR",
    val merchant: String = "",
    val dateTime: Long = System.currentTimeMillis(),
    val transactionType: TransactionType = TransactionType.UNKNOWN,
    val direction: TransactionDirection = TransactionDirection.DEBIT,
    val referenceNumber: String? = null,
    val accountInfo: String? = null,
    val status: TransactionStatus = TransactionStatus.SUCCESS,
    val sourceRaw: String = "",
    val source: TransactionSource = TransactionSource.SMS
)

/**
 * Domain model for budget tracking.
 */
data class Budget(
    val id: Long = 0,
    val categoryId: Long? = null,
    val category: Category? = null,
    val amount: Double,
    val spent: Double = 0.0,
    val month: Int,
    val year: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    val remaining: Double get() = amount - spent
    val percentage: Float get() = if (amount > 0) (spent / amount).toFloat().coerceIn(0f, 2f) else 0f
    val isExceeded: Boolean get() = spent > amount
    val isWarning: Boolean get() = percentage >= 0.8f && !isExceeded
}

/**
 * Domain model for recurring transactions.
 */
data class RecurringTransaction(
    val id: Long = 0,
    val amount: Double,
    val merchant: String,
    val category: Category = Category.OTHER,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDueDate: Long,
    val isActive: Boolean = true,
    val notes: String? = null
)

/**
 * Aggregated spending data for a category.
 */
data class CategorySpending(
    val category: Category,
    val totalAmount: Double,
    val transactionCount: Int,
    val percentage: Float = 0f
)

/**
 * Aggregated spending data for a merchant.
 */
data class MerchantSpending(
    val merchant: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val category: Category = Category.OTHER
)

/**
 * Dashboard summary data.
 */
data class DashboardSummary(
    val todaySpending: Double = 0.0,
    val weeklySpending: Double = 0.0,
    val monthlySpending: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val savingsEstimate: Double = 0.0,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val topMerchants: List<MerchantSpending> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList()
)

/**
 * Monthly trend data point.
 */
data class MonthlyTrend(
    val month: Int,
    val year: Int,
    val totalSpending: Double,
    val totalIncome: Double,
    val label: String = ""
)

/**
 * AI-generated spending insight.
 */
data class SpendingInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val relatedCategory: Category? = null,
    val relatedAmount: Double? = null
)

enum class InsightType {
    ANOMALY,
    TREND,
    SAVING_TIP,
    BUDGET_ALERT,
    SUMMARY
}

/**
 * Filter criteria for transaction search.
 */
data class TransactionFilter(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val categories: List<Category> = emptyList(),
    val merchants: List<String> = emptyList(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val transactionTypes: List<TransactionType> = emptyList(),
    val direction: TransactionDirection? = null,
    val searchQuery: String = ""
)
