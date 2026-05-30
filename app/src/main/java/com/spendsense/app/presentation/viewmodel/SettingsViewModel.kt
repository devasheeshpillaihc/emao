package com.spendsense.app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsense.app.data.source.SmsReader
import com.spendsense.app.domain.repository.TransactionRepository
import com.spendsense.app.domain.usecase.ProcessParsedTransactionUseCase
import com.spendsense.app.util.export.CsvExporter
import com.spendsense.app.util.export.PdfReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsReader: SmsReader,
    private val processParsedTransaction: ProcessParsedTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val csvExporter: CsvExporter,
    private val pdfReportGenerator: PdfReportGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            transactionRepository.getTransactionCount()
                .collect { count ->
                    _uiState.update { it.copy(transactionCount = count) }
                }
        }
    }

    fun scanSmsHistory(daysBack: Int = 30) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanningMessages = true, scanProgress = "Scanning SMS...") }

            try {
                val parsed = smsReader.scanSmsInbox(daysBack)
                var inserted = 0
                var duplicates = 0

                for (transaction in parsed) {
                    val result = processParsedTransaction(transaction)
                    result.fold(
                        onSuccess = { id ->
                            if (id > 0) inserted++ else duplicates++
                        },
                        onFailure = { /* skip */ }
                    )
                }

                _uiState.update {
                    it.copy(
                        isScanningMessages = false,
                        scanProgress = "Found ${parsed.size} transactions. Added $inserted new, $duplicates duplicates skipped."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanningMessages = false,
                        scanProgress = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                transactionRepository.getAllTransactions().first().let { transactions ->
                    val file = csvExporter.export(context, transactions)
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportMessage = "CSV exported to ${file.absolutePath}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, exportMessage = "Export failed: ${e.message}")
                }
            }
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                transactionRepository.getAllTransactions().first().let { transactions ->
                    val file = pdfReportGenerator.generateReport(context, transactions)
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportMessage = "PDF report exported to ${file.absolutePath}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, exportMessage = "Export failed: ${e.message}")
                }
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }
}

data class SettingsUiState(
    val transactionCount: Int = 0,
    val isDarkMode: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isScanningMessages: Boolean = false,
    val scanProgress: String? = null,
    val isExporting: Boolean = false,
    val exportMessage: String? = null
)
