package com.spendsense.app.data.repository

import com.spendsense.app.data.db.dao.BudgetDao
import com.spendsense.app.data.db.dao.TransactionDao
import com.spendsense.app.data.db.toDomain
import com.spendsense.app.data.db.toEntity
import com.spendsense.app.domain.model.Budget
import com.spendsense.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    override fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> {
        val (startDate, endDate) = getMonthRange(month, year)

        return combine(
            budgetDao.getBudgetsForMonth(month, year),
            transactionDao.getCategorySpending(startDate, endDate)
        ) { budgets, spending ->
            budgets.map { budget ->
                val spent = if (budget.categoryName != null) {
                    spending.find { it.category == budget.categoryName }?.total ?: 0.0
                } else {
                    spending.sumOf { it.total }
                }
                budget.toDomain(spent)
            }
        }
    }

    override fun getOverallBudget(month: Int, year: Int): Flow<Budget?> {
        val (startDate, endDate) = getMonthRange(month, year)

        return combine(
            budgetDao.getOverallBudget(month, year),
            transactionDao.getTotalSpending(startDate, endDate)
        ) { budget, totalSpent ->
            budget?.toDomain(totalSpent)
        }
    }

    override suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insert(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget.toEntity())
    }

    override suspend fun deleteBudget(id: Long) {
        budgetDao.deleteById(id)
    }

    private fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // Calendar months are 0-based
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val endDate = cal.timeInMillis
        return Pair(startDate, endDate)
    }
}
