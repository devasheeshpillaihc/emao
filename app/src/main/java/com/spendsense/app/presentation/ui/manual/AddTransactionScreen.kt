package com.spendsense.app.presentation.ui.manual

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
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
import com.spendsense.app.domain.model.*
import com.spendsense.app.presentation.theme.*
import com.spendsense.app.presentation.ui.components.CategoryChip
import com.spendsense.app.presentation.ui.components.getCategoryIcon
import com.spendsense.app.presentation.viewmodel.AddTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    transactionType: String = "expense",
    onNavigateBack: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate back on save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isExpense) "Add Expense" else "Add Income",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Income/Expense Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = uiState.isExpense,
                        onClick = { viewModel.toggleIsExpense(true) },
                        label = { Text("Expense") },
                        leadingIcon = {
                            if (uiState.isExpense) Icon(
                                Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    FilterChip(
                        selected = !uiState.isExpense,
                        onClick = { viewModel.toggleIsExpense(false) },
                        label = { Text("Income") },
                        leadingIcon = {
                            if (!uiState.isExpense) Icon(
                                Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Amount
            item {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::updateAmount,
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = CardShape,
                    leadingIcon = {
                        Text("₹", style = MaterialTheme.typography.titleLarge, color = Primary)
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    isError = uiState.error?.contains("amount", ignoreCase = true) == true
                )
            }

            // Merchant
            item {
                OutlinedTextField(
                    value = uiState.merchant,
                    onValueChange = viewModel::updateMerchant,
                    label = { Text("Merchant / Payee") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = CardShape,
                    leadingIcon = {
                        Icon(Icons.Filled.Store, contentDescription = null)
                    }
                )
            }

            // Category Selection
            item {
                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = if (uiState.isExpense) {
                        Category.entries.filter { it != Category.INCOME && it != Category.SALARY }
                    } else {
                        listOf(Category.INCOME, Category.SALARY, Category.TRANSFERS, Category.OTHER)
                    }
                    categories.forEach { category ->
                        val isSelected = uiState.selectedCategory == category
                        val color = CategoryColors.getColor(category)

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateCategory(category) },
                            label = { Text(category.displayName) },
                            leadingIcon = {
                                Icon(
                                    getCategoryIcon(category),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = color
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedLabelColor = color
                            )
                        )
                    }
                }
            }

            // Payment Method
            item {
                Text(
                    "Payment Method",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.entries.filter { it != TransactionType.UNKNOWN }.forEach { type ->
                        FilterChip(
                            selected = uiState.paymentMethod == type,
                            onClick = { viewModel.updateTransactionType(type) },
                            label = { Text(type.displayName) }
                        )
                    }
                }
            }

            // Notes
            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    minLines = 2,
                    maxLines = 4,
                    leadingIcon = {
                        Icon(Icons.Filled.Notes, contentDescription = null)
                    }
                )
            }

            // Tags
            item {
                OutlinedTextField(
                    value = uiState.tags,
                    onValueChange = viewModel::updateTags,
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = CardShape,
                    leadingIcon = {
                        Icon(Icons.Filled.Tag, contentDescription = null)
                    }
                )
            }

            // Recurring Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Recurring Transaction",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Repeat this transaction automatically",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isRecurring,
                        onCheckedChange = viewModel::toggleRecurring,
                        colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                    )
                }
            }

            // Recurring Frequency
            if (uiState.isRecurring) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RecurringFrequency.entries.forEach { freq ->
                            FilterChip(
                                selected = uiState.recurringFrequency == freq,
                                onClick = { viewModel.updateRecurringFrequency(freq) },
                                label = { Text(freq.displayName) }
                            )
                        }
                    }
                }
            }

            // Error Message
            if (uiState.error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.08f)),
                        shape = CardShape
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                uiState.error!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Error
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = viewModel::saveTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = ButtonShape,
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = DarkOnSurface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Save Transaction",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
