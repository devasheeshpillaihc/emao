package com.spendsense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.app.domain.model.*
import com.spendsense.app.domain.usecase.SearchTransactionsUseCase
import com.spendsense.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val searchTransactions: SearchTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(TransactionFilter())

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _activeFilter
            ) { query, filter ->
                Pair(query, filter)
            }.collectLatest { (query, filter) ->
                val flow = if (query.isNotBlank()) {
                    searchTransactions.search(query)
                } else if (filter != TransactionFilter()) {
                    searchTransactions.filter(filter)
                } else {
                    transactionRepository.getAllTransactions()
                }

                flow.catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }.collect { transactions ->
                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterChanged(filter: TransactionFilter) {
        _activeFilter.value = filter
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun clearFilters() {
        _activeFilter.value = TransactionFilter()
        _searchQuery.value = ""
        _uiState.update { it.copy(activeFilter = TransactionFilter(), searchQuery = "") }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }
}

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: TransactionFilter = TransactionFilter(),
    val isLoading: Boolean = true,
    val error: String? = null
)
