package com.spendsense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.app.domain.model.*
import com.spendsense.app.domain.usecase.BudgetAlert
import com.spendsense.app.domain.usecase.ManageBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val manageBudget: ManageBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            manageBudget.getBudgetsForCurrentMonth()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { budgets ->
                    _uiState.update { it.copy(budgets = budgets, isLoading = false) }
                }
        }

        viewModelScope.launch {
            manageBudget.getOverallBudget()
                .catch { }
                .collect { overall ->
                    _uiState.update { it.copy(overallBudget = overall) }
                }
        }

        viewModelScope.launch {
            manageBudget.getBudgetAlerts()
                .catch { }
                .collect { alerts ->
                    _uiState.update { it.copy(alerts = alerts) }
                }
        }
    }

    fun saveBudget(category: Category?, amount: Double) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val budget = Budget(
                category = category,
                amount = amount,
                month = cal.get(Calendar.MONTH) + 1,
                year = cal.get(Calendar.YEAR)
            )
            manageBudget.setBudget(budget)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            manageBudget.deleteBudget(id)
        }
    }

    fun showAddBudgetSheet(show: Boolean) {
        _uiState.update { it.copy(showAddBudgetSheet = show) }
    }
}

data class BudgetUiState(
    val budgets: List<Budget> = emptyList(),
    val overallBudget: Budget? = null,
    val alerts: List<BudgetAlert> = emptyList(),
    val showAddBudgetSheet: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
