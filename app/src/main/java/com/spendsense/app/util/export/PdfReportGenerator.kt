package com.spendsense.app.util.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.spendsense.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates PDF expense reports using Android's built-in PdfDocument API.
 * No external library dependencies.
 */
@Singleton
class PdfReportGenerator @Inject constructor() {

    companion object {
        private const val PAGE_WIDTH = 595  // A4 in points (72 dpi)
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 18f
    }

    /**
     * Generate a monthly expense report PDF.
     * @return The created PDF file
     */
    suspend fun generateReport(
        context: Context,
        transactions: List<Transaction>,
        title: String? = null
    ): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val reportTitle = title ?: "SpendSense Report"

        val exportDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "SpendSense"
        ).apply { mkdirs() }

        val file = File(exportDir, "SpendSense_Report_$timestamp.pdf")

        val document = PdfDocument()

        // ── Page 1: Summary ──
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(pageInfo1)
        drawSummaryPage(page1.canvas, reportTitle, transactions)
        document.finishPage(page1)

        // ── Page 2: Transaction List ──
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = document.startPage(pageInfo2)
        drawTransactionListPage(page2.canvas, transactions)
        document.finishPage(page2)

        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()

        file
    }

    private fun drawSummaryPage(canvas: Canvas, title: String, transactions: List<Transaction>) {
        val titlePaint = Paint().apply {
            color = Color.rgb(108, 99, 255) // Primary color
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(31, 35, 40)
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(101, 109, 118)
            textSize = 12f
            isAntiAlias = true
        }
        val amountPaint = Paint().apply {
            color = Color.rgb(31, 35, 40)
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(208, 215, 222)
            strokeWidth = 1f
        }

        var y = MARGIN + 30f

        // Title
        canvas.drawText(title, MARGIN, y, titlePaint)
        y += 10f

        // Date
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        canvas.drawText("Generated on ${dateFormat.format(Date())}", MARGIN, y + LINE_HEIGHT, bodyPaint)
        y += LINE_HEIGHT * 2 + 10f

        // Separator
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 20f

        // Summary Stats
        val debits = transactions.filter { it.direction == TransactionDirection.DEBIT && it.status == TransactionStatus.SUCCESS }
        val credits = transactions.filter { it.direction == TransactionDirection.CREDIT && it.status == TransactionStatus.SUCCESS }
        val totalSpent = debits.sumOf { it.amount }
        val totalIncome = credits.sumOf { it.amount }
        val savings = totalIncome - totalSpent

        canvas.drawText("SUMMARY", MARGIN, y, headerPaint)
        y += LINE_HEIGHT * 1.5f

        val stats = listOf(
            "Total Transactions" to "${transactions.size}",
            "Total Spending" to "₹${String.format("%,.2f", totalSpent)}",
            "Total Income" to "₹${String.format("%,.2f", totalIncome)}",
            "Net Savings" to "₹${String.format("%,.2f", savings)}"
        )

        for ((label, value) in stats) {
            canvas.drawText(label, MARGIN + 10f, y, bodyPaint)
            canvas.drawText(value, PAGE_WIDTH - MARGIN - 150f, y, amountPaint)
            y += LINE_HEIGHT * 1.3f
        }

        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 20f

        // Category Breakdown
        canvas.drawText("SPENDING BY CATEGORY", MARGIN, y, headerPaint)
        y += LINE_HEIGHT * 1.5f

        val byCategory = debits.groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .entries.sortedByDescending { it.value }

        for ((category, amount) in byCategory) {
            if (y > PAGE_HEIGHT - MARGIN * 2) break
            canvas.drawText(category.displayName, MARGIN + 10f, y, bodyPaint)
            canvas.drawText(
                "₹${String.format("%,.2f", amount)} (${debits.count { it.category == category }} txns)",
                PAGE_WIDTH - MARGIN - 200f, y, amountPaint
            )
            y += LINE_HEIGHT * 1.3f
        }

        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 20f

        // Top Merchants
        canvas.drawText("TOP MERCHANTS", MARGIN, y, headerPaint)
        y += LINE_HEIGHT * 1.5f

        val topMerchants = debits
            .groupBy { it.normalizedMerchant.ifBlank { it.merchant } }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .entries.sortedByDescending { it.value }
            .take(10)

        for ((merchant, amount) in topMerchants) {
            if (y > PAGE_HEIGHT - MARGIN) break
            val displayMerchant = if (merchant.length > 30) merchant.take(27) + "..." else merchant
            canvas.drawText(displayMerchant, MARGIN + 10f, y, bodyPaint)
            canvas.drawText("₹${String.format("%,.2f", amount)}", PAGE_WIDTH - MARGIN - 150f, y, amountPaint)
            y += LINE_HEIGHT * 1.3f
        }
    }

    private fun drawTransactionListPage(canvas: Canvas, transactions: List<Transaction>) {
        val headerPaint = Paint().apply {
            color = Color.rgb(31, 35, 40)
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(101, 109, 118)
            textSize = 10f
            isAntiAlias = true
        }
        val amountPaint = Paint().apply {
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(230, 237, 243)
            strokeWidth = 0.5f
        }

        var y = MARGIN + 20f

        canvas.drawText("TRANSACTION DETAILS", MARGIN, y, headerPaint)
        y += LINE_HEIGHT * 1.5f

        // Table header
        val cols = floatArrayOf(MARGIN, MARGIN + 70f, MARGIN + 200f, MARGIN + 300f, PAGE_WIDTH - MARGIN - 80f)
        canvas.drawText("Date", cols[0], y, headerPaint.apply { textSize = 9f })
        canvas.drawText("Merchant", cols[1], y, headerPaint)
        canvas.drawText("Category", cols[2], y, headerPaint)
        canvas.drawText("Type", cols[3], y, headerPaint)
        canvas.drawText("Amount", cols[4], y, headerPaint)
        y += 5f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        for (txn in transactions.take(40)) {
            if (y > PAGE_HEIGHT - MARGIN) break

            val merchant = txn.normalizedMerchant.ifBlank { txn.merchant }
            val displayMerchant = if (merchant.length > 20) merchant.take(17) + "..." else merchant

            canvas.drawText(dateFormat.format(Date(txn.dateTime)), cols[0], y, bodyPaint)
            canvas.drawText(displayMerchant, cols[1], y, bodyPaint)
            canvas.drawText(txn.category.displayName.take(15), cols[2], y, bodyPaint)
            canvas.drawText(txn.transactionType.displayName, cols[3], y, bodyPaint)

            amountPaint.color = when (txn.direction) {
                TransactionDirection.CREDIT -> Color.rgb(102, 187, 106)
                TransactionDirection.REFUND -> Color.rgb(0, 217, 166)
                TransactionDirection.DEBIT -> Color.rgb(255, 82, 82)
            }
            val prefix = if (txn.direction == TransactionDirection.DEBIT) "-" else "+"
            canvas.drawText(
                "$prefix₹${String.format("%.0f", txn.amount)}",
                cols[4], y, amountPaint
            )

            y += LINE_HEIGHT
            canvas.drawLine(MARGIN, y - 4f, PAGE_WIDTH - MARGIN, y - 4f, linePaint)
        }
    }
}
