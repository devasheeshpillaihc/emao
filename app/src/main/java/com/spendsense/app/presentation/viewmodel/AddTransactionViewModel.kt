package com.spendsense.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.app.domain.model.*
import com.spendsense.app.domain.usecase.AddManualTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addManualTransaction: AddManualTransactionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionType = savedStateHandle.get<String>("type") ?: "expense"

    private val _uiState = MutableStateFlow(
        AddTransactionUiState(
            isExpense = transactionType == "expense"
        )
    )
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateMerchant(merchant: String) {
        _uiState.update { it.copy(merchant = merchant) }
    }

    fun updateCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updateTags(tags: String) {
        _uiState.update { it.copy(tags = tags) }
    }

    fun updateDate(dateTime: Long) {
        _uiState.update { it.copy(dateTime = dateTime) }
    }

    fun updateTransactionType(type: TransactionType) {
        _uiState.update { it.copy(paymentMethod = type) }
    }

    fun toggleIsExpense(isExpense: Boolean) {
        _uiState.update { it.copy(isExpense = isExpense) }
    }

    fun toggleRecurring(isRecurring: Boolean) {
        _uiState.update { it.copy(isRecurring = isRecurring) }
    }

    fun updateRecurringFrequency(frequency: RecurringFrequency) {
        _uiState.update { it.copy(recurringFrequency = frequency) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Enter a valid amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val transaction = Transaction(
                amount = amount,
                merchant = state.merchant,
                normalizedMerchant = state.merchant,
                category = state.selectedCategory,
                transactionType = state.paymentMethod,
                direction = if (state.isExpense) TransactionDirection.DEBIT else TransactionDirection.CREDIT,
                dateTime = state.dateTime,
                source = TransactionSource.MANUAL,
                notes = state.notes.ifBlank { null },
                tags = state.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                isRecurring = state.isRecurring
            )

            val result = addManualTransaction(transaction)

            result.fold(
                onSuccess = { id ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = e.message ?: "Failed to save"
                        )
                    }
                }
            )
        }
    }
}

data class AddTransactionUiState(
    val amount: String = "",
    val merchant: String = "",
    val selectedCategory: Category = Category.OTHER,
    val notes: String = "",
    val tags: String = "",
    val dateTime: Long = System.currentTimeMillis(),
    val paymentMethod: TransactionType = TransactionType.UPI,
    val isExpense: Boolean = true,
    val isRecurring: Boolean = false,
    val recurringFrequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
