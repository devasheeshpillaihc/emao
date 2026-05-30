package com.spendsense.app.util.export

import android.content.Context
import android.os.Environment
import com.spendsense.app.domain.model.Transaction
import com.spendsense.app.presentation.ui.components.formatFullDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports transaction data to CSV format.
 */
@Singleton
class CsvExporter @Inject constructor() {

    companion object {
        private val CSV_HEADERS = listOf(
            "Date", "Time", "Merchant", "Category", "Amount (₹)",
            "Type", "Direction", "Payment Method", "Reference",
            "Account", "Notes", "Tags", "Status", "Source"
        )
    }

    /**
     * Export transactions to a CSV file.
     * @return The created CSV file
     */
    suspend fun export(
        context: Context,
        transactions: List<Transaction>,
        fileName: String? = null
    ): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val actualFileName = fileName ?: "SpendSense_Export_$timestamp.csv"

        val exportDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "SpendSense"
        ).apply { mkdirs() }

        val file = File(exportDir, actualFileName)

        FileWriter(file).use { writer ->
            // Write headers
            writer.appendLine(CSV_HEADERS.joinToString(","))

            // Write data rows
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            for (txn in transactions) {
                val date = Date(txn.dateTime)
                val row = listOf(
                    dateFormat.format(date),
                    timeFormat.format(date),
                    escapeCSV(txn.normalizedMerchant.ifBlank { txn.merchant }),
                    txn.category.displayName,
                    String.format("%.2f", txn.amount),
                    txn.transactionType.displayName,
                    txn.direction.name,
                    txn.transactionType.displayName,
                    txn.referenceNumber ?: "",
                    txn.accountInfo ?: "",
                    escapeCSV(txn.notes ?: ""),
                    escapeCSV(txn.tags.joinToString("; ")),
                    txn.status.name,
                    txn.source.name
                )
                writer.appendLine(row.joinToString(","))
            }
        }

        file
    }

    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
