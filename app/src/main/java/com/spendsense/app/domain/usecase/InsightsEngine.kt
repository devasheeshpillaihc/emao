package com.spendsense.app.domain.usecase

import com.spendsense.app.domain.model.*
import com.spendsense.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

/**
 * AI Insights Engine — generates spending insights, anomaly detection,
 * and smart summaries based on transaction history.
 */
class InsightsEngine @Inject constructor(
    private val transactionRepository: TransactionRepository
) {

    /**
     * Generate all insights for the current month.
     */
    fun getMonthlyInsights(): Flow<List<SpendingInsight>> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // Current month range
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val monthStart = cal.timeInMillis

        // Previous month range
        cal.add(Calendar.MONTH, -1)
        val prevMonthStart = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val prevMonthEnd = monthStart

        return transactionRepository.getTransactionsByDateRange(prevMonthStart, now)
            .map { allTransactions ->
                val currentMonth = allTransactions.filter { it.dateTime >= monthStart }
                val prevMonth = allTransactions.filter { it.dateTime < monthStart }

                val insights = mutableListOf<SpendingInsight>()

                // 1. Monthly spending summary
                insights.add(generateSpendingSummary(currentMonth))

                // 2. Anomaly detection
                insights.addAll(detectAnomalies(currentMonth))

                // 3. Category comparison with last month
                insights.addAll(compareCategoryTrends(currentMonth, prevMonth))

                // 4. Saving tips
                insights.addAll(generateSavingTips(currentMonth))

                // 5. Spending pattern insights
                insights.addAll(analyzeSpendingPatterns(currentMonth))

                insights
            }
    }

    private fun generateSpendingSummary(transactions: List<Transaction>): SpendingInsight {
        val totalSpent = transactions
            .filter { it.direction == TransactionDirection.DEBIT && it.status == TransactionStatus.SUCCESS }
            .sumOf { it.amount }
        val totalIncome = transactions
            .filter { it.direction == TransactionDirection.CREDIT && it.status == TransactionStatus.SUCCESS }
            .sumOf { it.amount }
        val txnCount = transactions.size

        return SpendingInsight(
            title = "Monthly Summary",
            description = "You've spent ₹${formatAmount(totalSpent)} across $txnCount transactions this month. " +
                    if (totalIncome > 0) "Income: ₹${formatAmount(totalIncome)}. " +
                            "Net savings: ₹${formatAmount(totalIncome - totalSpent)}" else "",
            type = InsightType.SUMMARY
        )
    }

    private fun detectAnomalies(transactions: List<Transaction>): List<SpendingInsight> {
        val insights = mutableListOf<SpendingInsight>()

        // Group by category and find average
        val categoryGroups = transactions
            .filter { it.direction == TransactionDirection.DEBIT }
            .groupBy { it.category }

        for ((category, txns) in categoryGroups) {
            if (txns.size < 2) continue

            val amounts = txns.map { it.amount }
            val average = amounts.average()
            val stdDev = calculateStdDev(amounts, average)

            // Find transactions > 2 standard deviations above average
            val anomalies = txns.filter { it.amount > average + 2 * stdDev }
            for (anomaly in anomalies) {
                insights.add(
                    SpendingInsight(
                        title = "Unusual Expense Detected",
                        description = "₹${formatAmount(anomaly.amount)} at ${anomaly.normalizedMerchant.ifBlank { anomaly.merchant }} " +
                                "is ${String.format("%.1f", anomaly.amount / average)}x your average ${category.displayName} expense.",
                        type = InsightType.ANOMALY,
                        relatedCategory = category,
                        relatedAmount = anomaly.amount
                    )
                )
            }
        }

        return insights
    }

    private fun compareCategoryTrends(
        currentMonth: List<Transaction>,
        prevMonth: List<Transaction>
    ): List<SpendingInsight> {
        val insights = mutableListOf<SpendingInsight>()

        val currentByCategory = currentMonth
            .filter { it.direction == TransactionDirection.DEBIT }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }

        val prevByCategory = prevMonth
            .filter { it.direction == TransactionDirection.DEBIT }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }

        for ((category, currentAmount) in currentByCategory) {
            val prevAmount = prevByCategory[category] ?: continue
            if (prevAmount <= 0) continue

            val changePercent = ((currentAmount - prevAmount) / prevAmount * 100)

            if (changePercent > 30) {
                insights.add(
                    SpendingInsight(
                        title = "${category.displayName} Spending Up",
                        description = "You're spending ${String.format("%.0f", changePercent)}% more on ${category.displayName} compared to last month " +
                                "(₹${formatAmount(currentAmount)} vs ₹${formatAmount(prevAmount)}).",
                        type = InsightType.TREND,
                        relatedCategory = category,
                        relatedAmount = currentAmount
                    )
                )
            } else if (changePercent < -30) {
                insights.add(
                    SpendingInsight(
                        title = "${category.displayName} Spending Down 🎉",
                        description = "Great job! You've reduced ${category.displayName} spending by ${String.format("%.0f", -changePercent)}% " +
                                "(₹${formatAmount(currentAmount)} vs ₹${formatAmount(prevAmount)} last month).",
                        type = InsightType.SAVING_TIP,
                        relatedCategory = category,
                        relatedAmount = currentAmount
                    )
                )
            }
        }

        return insights
    }

    private fun generateSavingTips(transactions: List<Transaction>): List<SpendingInsight> {
        val insights = mutableListOf<SpendingInsight>()

        val debits = transactions.filter { it.direction == TransactionDirection.DEBIT }
        val byCategory = debits.groupBy { it.category }.mapValues { it.value.sumOf { t -> t.amount } }

        // Find the highest spending category
        val topCategory = byCategory.maxByOrNull { it.value }
        if (topCategory != null && topCategory.value > 0) {
            val totalSpent = debits.sumOf { it.amount }
            val percentage = topCategory.value / totalSpent * 100

            if (percentage > 40) {
                insights.add(
                    SpendingInsight(
                        title = "Consider Reducing ${topCategory.key.displayName}",
                        description = "${topCategory.key.displayName} accounts for ${String.format("%.0f", percentage)}% of your total spending. " +
                                "Reducing it by even 10% could save ₹${formatAmount(topCategory.value * 0.1)}/month.",
                        type = InsightType.SAVING_TIP,
                        relatedCategory = topCategory.key,
                        relatedAmount = topCategory.value
                    )
                )
            }
        }

        // Subscription check
        val subscriptionSpend = byCategory[Category.SUBSCRIPTIONS] ?: 0.0
        if (subscriptionSpend > 1000) {
            insights.add(
                SpendingInsight(
                    title = "Review Subscriptions",
                    description = "You're spending ₹${formatAmount(subscriptionSpend)} on subscriptions. Review if all are still needed.",
                    type = InsightType.SAVING_TIP,
                    relatedCategory = Category.SUBSCRIPTIONS,
                    relatedAmount = subscriptionSpend
                )
            )
        }

        return insights
    }

    private fun analyzeSpendingPatterns(transactions: List<Transaction>): List<SpendingInsight> {
        val insights = mutableListOf<SpendingInsight>()

        val debits = transactions.filter { it.direction == TransactionDirection.DEBIT }
        if (debits.isEmpty()) return insights

        // Weekday vs Weekend spending
        val cal = Calendar.getInstance()
        val weekdaySpending = debits.filter {
            cal.timeInMillis = it.dateTime
            val day = cal.get(Calendar.DAY_OF_WEEK)
            day != Calendar.SATURDAY && day != Calendar.SUNDAY
        }.sumOf { it.amount }

        val weekendSpending = debits.filter {
            cal.timeInMillis = it.dateTime
            val day = cal.get(Calendar.DAY_OF_WEEK)
            day == Calendar.SATURDAY || day == Calendar.SUNDAY
        }.sumOf { it.amount }

        val weekdayDays = 22 // approx working days in a month
        val weekendDays = 8
        val avgWeekday = if (weekdayDays > 0) weekdaySpending / weekdayDays else 0.0
        val avgWeekend = if (weekendDays > 0) weekendSpending / weekendDays else 0.0

        if (avgWeekend > avgWeekday * 1.5 && avgWeekend > 0) {
            insights.add(
                SpendingInsight(
                    title = "Weekend Spender 🛍️",
                    description = "You spend ${String.format("%.0f", (avgWeekend / avgWeekday - 1) * 100)}% more on weekends " +
                            "(₹${formatAmount(avgWeekend)}/day) vs weekdays (₹${formatAmount(avgWeekday)}/day).",
                    type = InsightType.TREND
                )
            )
        }

        return insights
    }

    private fun calculateStdDev(values: List<Double>, mean: Double): Double {
        if (values.size < 2) return 0.0
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return Math.sqrt(variance)
    }

    private fun formatAmount(amount: Double): String {
        return when {
            amount >= 10_000_000 -> String.format("%.1fCr", amount / 10_000_000)
            amount >= 100_000 -> String.format("%.1fL", amount / 100_000)
            amount >= 1000 -> String.format("%.0f", amount)
            else -> String.format("%.2f", amount)
        }
    }
}
