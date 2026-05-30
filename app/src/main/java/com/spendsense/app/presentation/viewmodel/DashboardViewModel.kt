package com.spendsense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.app.domain.model.DashboardSummary
import com.spendsense.app.domain.model.SpendingInsight
import com.spendsense.app.domain.usecase.GetDashboardDataUseCase
import com.spendsense.app.domain.usecase.InsightsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardData: GetDashboardDataUseCase,
    private val insightsEngine: InsightsEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
        loadInsights()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            getDashboardData()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { summary ->
                    _uiState.update {
                        it.copy(
                            summary = summary,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadInsights() {
        viewModelScope.launch {
            insightsEngine.getMonthlyInsights()
                .catch { /* Silently ignore insight errors */ }
                .collect { insights ->
                    _uiState.update { it.copy(insights = insights) }
                }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadDashboard()
        loadInsights()
    }
}

data class DashboardUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val insights: List<SpendingInsight> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
