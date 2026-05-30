package com.spendsense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.app.domain.model.*
import com.spendsense.app.domain.usecase.GetAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalytics: GetAnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        val cal = Calendar.getInstance()
        val endDate = cal.timeInMillis

        // Current month
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val monthStart = cal.timeInMillis

        viewModelScope.launch {
            // Category spending for current month
            getAnalytics.getCategorySpending(monthStart, endDate)
                .catch { }
                .collect { spending ->
                    _uiState.update { it.copy(categorySpending = spending, isLoading = false) }
                }
        }

        viewModelScope.launch {
            // Monthly trends (last 6 months)
            getAnalytics.getMonthlyTrends(6)
                .catch { }
                .collect { trends ->
                    _uiState.update { it.copy(monthlyTrends = trends) }
                }
        }

        viewModelScope.launch {
            // Top merchants for current month
            getAnalytics.getTopMerchants(monthStart, endDate, 10)
                .catch { }
                .collect { merchants ->
                    _uiState.update { it.copy(topMerchants = merchants) }
                }
        }
    }

    fun selectTab(tab: AnalyticsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}

data class AnalyticsUiState(
    val categorySpending: List<CategorySpending> = emptyList(),
    val monthlyTrends: List<MonthlyTrend> = emptyList(),
    val topMerchants: List<MerchantSpending> = emptyList(),
    val selectedTab: AnalyticsTab = AnalyticsTab.OVERVIEW,
    val isLoading: Boolean = true
)

enum class AnalyticsTab(val title: String) {
    OVERVIEW("Overview"),
    CATEGORIES("Categories"),
    MERCHANTS("Merchants"),
    TRENDS("Trends")
}
