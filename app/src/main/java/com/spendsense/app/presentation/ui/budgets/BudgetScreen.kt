package com.spendsense.app.presentation.ui.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spendsense.app.domain.model.Category
import com.spendsense.app.presentation.theme.*
import com.spendsense.app.presentation.ui.components.*
import com.spendsense.app.presentation.viewmodel.BudgetViewModel
import com.spendsense.app.domain.usecase.BudgetAlertType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddBudgetSheet(true) },
                containerColor = Primary,
                contentColor = DarkOnSurface
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Budget")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Overall Budget
            item {
                uiState.overallBudget?.let { budget ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Primary.copy(alpha = 0.08f)
                        ),
                        shape = CardShape
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Overall Monthly Budget",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${String.format("%.0f", budget.percentage * 100)}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        budget.isExceeded -> Error
                                        budget.isWarning -> Warning
                                        else -> Primary
                                    }
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            BudgetProgressBar(
                                label = "",
                                spent = budget.spent,
                                budget = budget.amount,
                                color = Primary
                            )
                        }
                    }
                }
            }

            // Alerts
            if (uiState.alerts.isNotEmpty()) {
                item {
                    Text(
                        "Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.alerts) { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (alert.type) {
                                BudgetAlertType.EXCEEDED -> Error.copy(alpha = 0.08f)
                                BudgetAlertType.WARNING -> Warning.copy(alpha = 0.08f)
                            }
                        ),
                        shape = CardShape
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (alert.type) {
                                    BudgetAlertType.EXCEEDED -> Icons.Filled.ErrorOutline
                                    BudgetAlertType.WARNING -> Icons.Filled.Warning
                                },
                                contentDescription = null,
                                tint = when (alert.type) {
                                    BudgetAlertType.EXCEEDED -> Error
                                    BudgetAlertType.WARNING -> Warning
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                alert.message,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Category Budgets
            if (uiState.budgets.isNotEmpty()) {
                item {
                    Text(
                        "Category Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.budgets.filter { it.category != null }) { budget ->
                    val color = CategoryColors.getColor(budget.category!!)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = CardShape
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    getCategoryIcon(budget.category),
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    budget.category.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.deleteBudget(budget.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            BudgetProgressBar(
                                label = "",
                                spent = budget.spent,
                                budget = budget.amount,
                                color = color
                            )
                        }
                    }
                }
            }

            if (uiState.budgets.isEmpty() && uiState.overallBudget == null) {
                item {
                    EmptyState(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = "No budgets set",
                        description = "Set monthly budgets to track your spending limits."
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }

        // Add Budget Bottom Sheet
        if (uiState.showAddBudgetSheet) {
            AddBudgetSheet(
                onSave = { category, amount ->
                    viewModel.saveBudget(category, amount)
                    viewModel.showAddBudgetSheet(false)
                },
                onDismiss = { viewModel.showAddBudgetSheet(false) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetSheet(
    onSave: (Category?, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var amount by remember { mutableStateOf("") }
    var isOverall by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = BottomSheetShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Set Budget",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Budget Type Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = isOverall,
                    onClick = { isOverall = true; selectedCategory = null },
                    label = { Text("Overall") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = !isOverall,
                    onClick = { isOverall = false },
                    label = { Text("Category") }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Category Selection (if not overall)
            if (!isOverall) {
                Text("Select Category", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Category.entries
                        .filter { it != Category.INCOME && it != Category.SALARY }
                        .forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CategoryColors.getColor(category).copy(alpha = 0.2f)
                                )
                            )
                        }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Budget Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = CardShape
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val budgetAmount = amount.toDoubleOrNull() ?: return@Button
                    if (budgetAmount > 0) {
                        onSave(if (isOverall) null else selectedCategory, budgetAmount)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = ButtonShape,
                enabled = amount.toDoubleOrNull()?.let { it > 0 } == true &&
                        (isOverall || selectedCategory != null)
            ) {
                Text("Save Budget", modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
