package com.spendsense.app.domain.usecase

import com.spendsense.app.domain.model.*
import com.spendsense.app.domain.repository.BudgetRepository
import com.spendsense.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

/**
 * Aggregates all dashboard data into a single observable flow.
 */
class GetDashboardDataUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(): Flow<DashboardSummary> {
        val now = Calendar.getInstance()
        val todayStart = getStartOfDay(now)
        val weekStart = getStartOfWeek(now)
        val monthStart = getStartOfMonth(now)
        val endOfDay = todayStart + 24 * 60 * 60 * 1000L

        return combine(
            transactionRepository.getTotalSpending(todayStart, endOfDay),
            transactionRepository.getTotalSpending(weekStart, endOfDay),
            transactionRepository.getTotalSpending(monthStart, endOfDay),
            transactionRepository.getTotalIncome(monthStart, endOfDay),
            transactionRepository.getCategorySpending(monthStart, endOfDay),
        ) { todaySpend, weekSpend, monthSpend, monthIncome, categoryBreakdown ->
            DashboardSummary(
                todaySpending = todaySpend,
                weeklySpending = weekSpend,
                monthlySpending = monthSpend,
                monthlyIncome = monthIncome,
                savingsEstimate = monthIncome - monthSpend,
                categoryBreakdown = categoryBreakdown,
            )
        }.combine(
            transactionRepository.getTopMerchants(monthStart, endOfDay, 5)
        ) { summary, merchants ->
            summary.copy(topMerchants = merchants)
        }.combine(
            transactionRepository.getRecentTransactions(5)
        ) { summary, recent ->
            summary.copy(recentTransactions = recent)
        }
    }

    private fun getStartOfDay(cal: Calendar): Long {
        return Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getStartOfWeek(cal: Calendar): Long {
        return Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getStartOfMonth(cal: Calendar): Long {
        return Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

/**
 * Handles manual transaction creation with validation.
 */
class AddManualTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        // Validate
        if (transaction.amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be positive"))
        }
        if (transaction.merchant.isBlank() && transaction.notes.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Merchant or notes required"))
        }

        return try {
            val id = transactionRepository.insertTransaction(
                transaction.copy(source = TransactionSource.MANUAL)
            )
            if (id == -1L) {
                Result.failure(IllegalStateException("Duplicate transaction detected"))
            } else {
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Processes parsed transactions from SMS/notifications into the database.
 */
class ProcessParsedTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(parsed: ParsedTransaction): Result<Long> {
        return try {
            val id = transactionRepository.insertParsedTransaction(parsed)
            if (id == -1L) {
                Result.success(-1) // Duplicate, not an error
            } else {
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Gets analytics data for charts and trends.
 */
class GetAnalyticsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    fun getCategorySpending(startDate: Long, endDate: Long): Flow<List<CategorySpending>> {
        return transactionRepository.getCategorySpending(startDate, endDate)
    }

    fun getMonthlyTrends(monthsBack: Int = 6): Flow<List<MonthlyTrend>> {
        val now = Calendar.getInstance()
        val endDate = now.timeInMillis
        now.add(Calendar.MONTH, -monthsBack)
        val startDate = now.timeInMillis
        return transactionRepository.getMonthlyTrends(startDate, endDate)
    }

    fun getTopMerchants(startDate: Long, endDate: Long, limit: Int = 10): Flow<List<MerchantSpending>> {
        return transactionRepository.getTopMerchants(startDate, endDate, limit)
    }
}

/**
 * Budget management use case with alert checking.
 */
class ManageBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    fun getBudgetsForCurrentMonth(): Flow<List<Budget>> {
        val cal = Calendar.getInstance()
        return budgetRepository.getBudgetsForMonth(
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

    fun getOverallBudget(): Flow<Budget?> {
        val cal = Calendar.getInstance()
        return budgetRepository.getOverallBudget(
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

    suspend fun setBudget(budget: Budget): Long {
        return budgetRepository.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetRepository.updateBudget(budget)
    }

    suspend fun deleteBudget(id: Long) {
        budgetRepository.deleteBudget(id)
    }

    /**
     * Check which budgets need alerts.
     */
    fun getBudgetAlerts(): Flow<List<BudgetAlert>> {
        return getBudgetsForCurrentMonth().map { budgets ->
            budgets.mapNotNull { budget ->
                when {
                    budget.isExceeded -> BudgetAlert(
                        budget = budget,
                        type = BudgetAlertType.EXCEEDED,
                        message = "${budget.category?.displayName ?: "Overall"} budget exceeded by ₹${String.format("%.0f", budget.spent - budget.amount)}"
                    )
                    budget.isWarning -> BudgetAlert(
                        budget = budget,
                        type = BudgetAlertType.WARNING,
                        message = "${budget.category?.displayName ?: "Overall"} budget is ${String.format("%.0f", budget.percentage * 100)}% used"
                    )
                    else -> null
                }
            }
        }
    }
}

data class BudgetAlert(
    val budget: Budget,
    val type: BudgetAlertType,
    val message: String
)

enum class BudgetAlertType {
    WARNING, EXCEEDED
}

/**
 * Search and filter transactions.
 */
class SearchTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    fun search(query: String): Flow<List<Transaction>> {
        return transactionRepository.searchTransactions(query)
    }

    fun filter(filter: TransactionFilter): Flow<List<Transaction>> {
        return transactionRepository.getFilteredTransactions(filter)
    }
}

/**
 * Export transactions to various formats.
 */
class ExportDataUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    fun getTransactionsForExport(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByDateRange(startDate, endDate)
    }
}
