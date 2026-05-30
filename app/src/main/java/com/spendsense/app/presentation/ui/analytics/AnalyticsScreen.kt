package com.spendsense.app.presentation.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.spendsense.app.presentation.theme.*
import com.spendsense.app.presentation.ui.components.*
import com.spendsense.app.presentation.viewmodel.AnalyticsTab
import com.spendsense.app.presentation.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Analytics", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = AnalyticsTab.entries.indexOf(uiState.selectedTab),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Primary,
                divider = {}
            ) {
                AnalyticsTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.title) }
                    )
                }
            }

            when (uiState.selectedTab) {
                AnalyticsTab.OVERVIEW -> OverviewTab(viewModel)
                AnalyticsTab.CATEGORIES -> CategoriesTab(viewModel)
                AnalyticsTab.MERCHANTS -> MerchantsTab(viewModel)
                AnalyticsTab.TRENDS -> TrendsTab(viewModel)
            }
        }
    }
}

@Composable
private fun OverviewTab(viewModel: AnalyticsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category Spending Summary
        item {
            Text(
                "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.categorySpending.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.PieChart,
                    title = "No spending data",
                    description = "Add transactions to see your spending breakdown."
                )
            }
        } else {
            // Category bars
            items(uiState.categorySpending) { catSpending ->
                val color = CategoryColors.getColor(catSpending.category)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                getCategoryIcon(catSpending.category),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                catSpending.category.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "₹${formatAmount(catSpending.totalAmount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${catSpending.transactionCount} txns • ${String.format("%.1f", catSpending.percentage)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (catSpending.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = color,
                        trackColor = color.copy(alpha = 0.12f),
                    )
                }
            }
        }

        // Monthly Trends
        if (uiState.monthlyTrends.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Monthly Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(uiState.monthlyTrends) { trend ->
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
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            trend.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Row {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "↓ ₹${formatAmount(trend.totalIncome)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Success
                                )
                                Text(
                                    "↑ ₹${formatAmount(trend.totalSpending)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriesTab(viewModel: AnalyticsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(uiState.categorySpending) { catSpending ->
            val color = CategoryColors.getColor(catSpending.category)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = color.copy(alpha = 0.08f)
                ),
                shape = CardShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        getCategoryIcon(catSpending.category),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            catSpending.category.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${catSpending.transactionCount} transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "₹${formatAmount(catSpending.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            "${String.format("%.1f", catSpending.percentage)}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MerchantsTab(viewModel: AnalyticsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (uiState.topMerchants.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Store,
                    title = "No merchant data",
                    description = "Start tracking transactions to see your top merchants."
                )
            }
        } else {
            items(uiState.topMerchants) { merchant ->
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
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val color = CategoryColors.getColor(merchant.category)
                        Icon(
                            getCategoryIcon(merchant.category),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                merchant.merchant,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            CategoryChip(category = merchant.category, small = true)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "₹${formatAmount(merchant.totalAmount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${merchant.transactionCount} txns",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendsTab(viewModel: AnalyticsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.monthlyTrends.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.ShowChart,
                    title = "No trend data yet",
                    description = "Track expenses for a few months to see spending trends."
                )
            }
        } else {
            item {
                Text(
                    "6-Month Spending Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(uiState.monthlyTrends) { trend ->
                val maxSpending = uiState.monthlyTrends.maxOf { it.totalSpending }.coerceAtLeast(1.0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = CardShape
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                trend.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "₹${formatAmount(trend.totalSpending)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (trend.totalSpending / maxSpending).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Primary,
                            trackColor = Primary.copy(alpha = 0.12f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Income: ₹${formatAmount(trend.totalIncome)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Success
                            )
                            val savings = trend.totalIncome - trend.totalSpending
                            Text(
                                "Savings: ₹${formatAmount(savings)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (savings >= 0) Secondary else Error
                            )
                        }
                    }
                }
            }
        }
    }
}
