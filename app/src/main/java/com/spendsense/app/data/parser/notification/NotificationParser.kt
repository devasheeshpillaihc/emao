package com.spendsense.app.data.parser.notification

import com.spendsense.app.data.parser.TransactionParser
import com.spendsense.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses transaction notifications from UPI apps and bank apps.
 * Handles Google Pay, PhonePe, Paytm, and bank app notification formats.
 */
@Singleton
class NotificationParser @Inject constructor() : TransactionParser {

    companion object {
        // Package names for monitored apps
        val MONITORED_PACKAGES = setOf(
            "com.google.android.apps.nbu.paisa.user",  // Google Pay
            "com.phonepe.app",                           // PhonePe
            "net.one97.paytm",                           // Paytm
            "in.org.npci.upiapp",                        // BHIM
            "com.whatsapp",                               // WhatsApp Pay
            "in.amazon.mShop.android.shopping",           // Amazon Pay
            // Bank apps
            "com.sbi.SBIFreedomPlus",                    // SBI YONO
            "com.csam.icici.bank.imobile",               // iMobile
            "com.snapwork.hdfc",                          // HDFC
            "com.axis.mobile",                            // Axis
            "com.msf.kbank.mobile",                       // Kotak
        )

        // Google Pay patterns
        private val GPAY_PAID_PATTERN = Regex(
            """(?:Paid|Sent)\s*₹?\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:to)\s+(.+?)(?:\s*$|\s*\.)""",
            RegexOption.IGNORE_CASE
        )
        private val GPAY_RECEIVED_PATTERN = Regex(
            """(?:Received)\s*₹?\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:from)\s+(.+?)(?:\s*$|\s*\.)""",
            RegexOption.IGNORE_CASE
        )

        // PhonePe patterns
        private val PHONEPE_PAID_PATTERN = Regex(
            """(?:Paid|Sent)\s*₹?\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:to)\s+(.+?)(?:\s*successfully|\s*via|\s*$)""",
            RegexOption.IGNORE_CASE
        )
        private val PHONEPE_RECEIVED_PATTERN = Regex(
            """(?:Received)\s*₹?\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:from)\s+(.+?)(?:\s*$)""",
            RegexOption.IGNORE_CASE
        )

        // Paytm patterns
        private val PAYTM_PAID_PATTERN = Regex(
            """(?:Paid|Sent|Money sent)\s*₹?\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:to)\s+(.+?)(?:\s*$|\s*\.)""",
            RegexOption.IGNORE_CASE
        )
        private val PAYTM_RECEIVED_PATTERN = Regex(
            """(?:Received|Money received)\s*₹?\s*([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:from)\s+(.+?)(?:\s*$)""",
            RegexOption.IGNORE_CASE
        )

        // Generic UPI notification pattern
        private val GENERIC_AMOUNT_PATTERN = Regex(
            """₹\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        )

        // UPI Reference in notifications
        private val NOTIFICATION_REF_PATTERN = Regex(
            """(?:UPI\s*(?:Ref|ID|Txn)[:\s]*|Ref\s*(?:No\.?\s*)?[:\s]*)(\d{10,})""",
            RegexOption.IGNORE_CASE
        )

        // Failed transaction detection
        private val FAILED_PATTERN = Regex(
            """(?:failed|declined|unsuccessful|couldn'?t\s*(?:be\s*)?(?:completed|processed))""",
            RegexOption.IGNORE_CASE
        )
    }

    override fun parse(rawText: String, source: TransactionSource, sender: String): ParsedTransaction? {
        if (rawText.isBlank()) return null

        // Try app-specific parsing first
        val result = when {
            sender.contains("nbu.paisa", ignoreCase = true) -> parseGooglePay(rawText)
            sender.contains("phonepe", ignoreCase = true) -> parsePhonePe(rawText)
            sender.contains("paytm", ignoreCase = true) -> parsePaytm(rawText)
            else -> parseGeneric(rawText)
        }

        return result?.copy(
            source = TransactionSource.NOTIFICATION,
            sourceRaw = rawText,
            status = if (FAILED_PATTERN.containsMatchIn(rawText)) TransactionStatus.FAILED else TransactionStatus.SUCCESS
        )
    }

    private fun parseGooglePay(text: String): ParsedTransaction? {
        // Try paid pattern
        GPAY_PAID_PATTERN.find(text)?.let { match ->
            return createParsedTransaction(
                amount = parseAmount(match.groupValues[1]),
                merchant = match.groupValues[2].trim(),
                direction = TransactionDirection.DEBIT,
                text = text
            )
        }

        // Try received pattern
        GPAY_RECEIVED_PATTERN.find(text)?.let { match ->
            return createParsedTransaction(
                amount = parseAmount(match.groupValues[1]),
                merchant = match.groupValues[2].trim(),
                direction = TransactionDirection.CREDIT,
                text = text
            )
        }

        return parseGeneric(text)
    }

    private fun parsePhonePe(text: String): ParsedTransaction? {
        PHONEPE_PAID_PATTERN.find(text)?.let { match ->
            return createParsedTransaction(
                amount = parseAmount(match.groupValues[1]),
                merchant = match.groupValues[2].trim(),
                direction = TransactionDirection.DEBIT,
                text = text
            )
        }

        PHONEPE_RECEIVED_PATTERN.find(text)?.let { match ->
            return createParsedTransaction(
                amount = parseAmount(match.groupValues[1]),
                merchant = match.groupValues[2].trim(),
                direction = TransactionDirection.CREDIT,
                text = text
            )
        }

        return parseGeneric(text)
    }

    private fun parsePaytm(text: String): ParsedTransaction? {
        PAYTM_PAID_PATTERN.find(text)?.let { match ->
            return createParsedTransaction(
                amount = parseAmount(match.groupValues[1]),
                merchant = match.groupValues[2].trim(),
                direction = TransactionDirection.DEBIT,
                text = text
            )
        }

        PAYTM_RECEIVED_PATTERN.find(text)?.let { match ->
            return createParsedTransaction(
                amount = parseAmount(match.groupValues[1]),
                merchant = match.groupValues[2].trim(),
                direction = TransactionDirection.CREDIT,
                text = text
            )
        }

        return parseGeneric(text)
    }

    private fun parseGeneric(text: String): ParsedTransaction? {
        val amountMatch = GENERIC_AMOUNT_PATTERN.find(text) ?: return null
        val amount = parseAmount(amountMatch.groupValues[1])
        if (amount <= 0) return null

        val direction = when {
            text.contains(Regex("""(?:paid|sent|debited)""", RegexOption.IGNORE_CASE)) -> TransactionDirection.DEBIT
            text.contains(Regex("""(?:received|credited)""", RegexOption.IGNORE_CASE)) -> TransactionDirection.CREDIT
            text.contains(Regex("""refund""", RegexOption.IGNORE_CASE)) -> TransactionDirection.REFUND
            else -> TransactionDirection.DEBIT
        }

        // Try to extract merchant
        val merchantPattern = Regex("""(?:to|from|at)\s+([A-Za-z0-9][A-Za-z0-9\s&._'-]{1,30})""", RegexOption.IGNORE_CASE)
        val merchant = merchantPattern.find(text)?.groupValues?.get(1)?.trim() ?: ""

        return createParsedTransaction(amount, merchant, direction, text)
    }

    private fun createParsedTransaction(
        amount: Double,
        merchant: String,
        direction: TransactionDirection,
        text: String
    ): ParsedTransaction? {
        if (amount <= 0) return null

        val refNumber = NOTIFICATION_REF_PATTERN.find(text)?.groupValues?.get(1)

        return ParsedTransaction(
            amount = amount,
            merchant = merchant,
            direction = direction,
            transactionType = TransactionType.UPI,
            referenceNumber = refNumber,
            dateTime = System.currentTimeMillis()
        )
    }

    private fun parseAmount(amountStr: String): Double {
        return amountStr.replace(",", "").toDoubleOrNull() ?: 0.0
    }

    /**
     * Check if a notification should be monitored.
     */
    fun shouldMonitor(packageName: String): Boolean {
        return packageName in MONITORED_PACKAGES
    }
}
