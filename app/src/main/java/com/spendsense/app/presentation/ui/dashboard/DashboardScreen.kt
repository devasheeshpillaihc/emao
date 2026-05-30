package com.spendsense.app.presentation.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spendsense.app.domain.model.InsightType
import com.spendsense.app.presentation.theme.*
import com.spendsense.app.presentation.ui.components.*
import com.spendsense.app.presentation.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToAddTransaction: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddTransaction("expense") },
                containerColor = Primary,
                contentColor = DarkOnSurface
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──
            item {
                Column {
                    Text(
                        text = "SpendSense",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = dateFormat.format(Date()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Spending Cards Row ──
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SpendingCard(
                            title = "Today",
                            amount = uiState.summary.todaySpending,
                            icon = Icons.Filled.Today,
                            gradientColors = listOf(GradientPurpleStart, GradientPurpleEnd),
                            modifier = Modifier.width(170.dp)
                        )
                    }
                    item {
                        SpendingCard(
                            title = "This Week",
                            amount = uiState.summary.weeklySpending,
                            icon = Icons.Filled.DateRange,
                            gradientColors = listOf(GradientTealStart, GradientTealEnd),
                            modifier = Modifier.width(170.dp)
                        )
                    }
                    item {
                        SpendingCard(
                            title = "This Month",
                            amount = uiState.summary.monthlySpending,
                            icon = Icons.Filled.CalendarMonth,
                            gradientColors = listOf(GradientAmberStart, GradientAmberEnd),
                            modifier = Modifier.width(170.dp)
                        )
                    }
                }
            }

            // ── Income vs Expense Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = CardShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ArrowDownward,
                                contentDescription = "Income",
                                tint = Success,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("Income", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "₹${formatAmount(uiState.summary.monthlyIncome)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(60.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ArrowUpward,
                                contentDescription = "Expense",
                                tint = Error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("Expense", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "₹${formatAmount(uiState.summary.monthlySpending)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Error
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(60.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Savings,
                                contentDescription = "Savings",
                                tint = Secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("Savings", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "₹${formatAmount(uiState.summary.savingsEstimate)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Secondary
                            )
                        }
                    }
                }
            }

            // ── Category Breakdown ──
            if (uiState.summary.categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = "Spending by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.summary.categoryBreakdown.take(5)) { categorySpending ->
                    val color = CategoryColors.getColor(categorySpending.category)
                    BudgetProgressBar(
                        label = categorySpending.category.displayName,
                        spent = categorySpending.totalAmount,
                        budget = uiState.summary.monthlySpending.coerceAtLeast(1.0),
                        color = color
                    )
                }
            }

            // ── AI Insights ──
            if (uiState.insights.isNotEmpty()) {
                item {
                    Text(
                        text = "Smart Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.insights.take(3)) { insight ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (insight.type) {
                                InsightType.ANOMALY -> Error.copy(alpha = 0.08f)
                                InsightType.SAVING_TIP -> Success.copy(alpha = 0.08f)
                                InsightType.BUDGET_ALERT -> Warning.copy(alpha = 0.08f)
                                else -> Primary.copy(alpha = 0.08f)
                            }
                        ),
                        shape = CardShape
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = when (insight.type) {
                                    InsightType.ANOMALY -> Icons.Filled.Warning
                                    InsightType.SAVING_TIP -> Icons.Filled.Lightbulb
                                    InsightType.BUDGET_ALERT -> Icons.Filled.NotificationsActive
                                    InsightType.TREND -> Icons.Filled.TrendingUp
                                    InsightType.SUMMARY -> Icons.Filled.Assessment
                                },
                                contentDescription = null,
                                tint = when (insight.type) {
                                    InsightType.ANOMALY -> Error
                                    InsightType.SAVING_TIP -> Success
                                    InsightType.BUDGET_ALERT -> Warning
                                    else -> Primary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = insight.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = insight.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Recent Transactions ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("See all")
                    }
                }
            }

            if (uiState.summary.recentTransactions.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.ReceiptLong,
                        title = "No transactions yet",
                        description = "Your expenses will appear here once detected from SMS or notifications."
                    )
                }
            } else {
                items(
                    items = uiState.summary.recentTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionCard(transaction = transaction)
                }
            }

            // Bottom spacer for FAB
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}
