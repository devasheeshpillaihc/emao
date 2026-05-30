package com.spendsense.app.data.parser.sms

import com.spendsense.app.data.parser.TransactionParser
import com.spendsense.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses bank SMS messages to extract transaction details.
 * Uses SmsBankPatterns for regex-based extraction across multiple Indian bank formats.
 */
@Singleton
class SmsTransactionParser @Inject constructor() : TransactionParser {

    override fun parse(rawText: String, source: TransactionSource, sender: String): ParsedTransaction? {
        if (rawText.isBlank()) return null

        // Extract amount — this is the most critical field
        val amount = SmsBankPatterns.extractAmount(rawText) ?: return null

        // Skip very small or suspiciously large amounts
        if (amount <= 0 || amount > 100_000_000) return null

        // Detect if this is a failed transaction
        val status = if (SmsBankPatterns.isFailedTransaction(rawText)) {
            TransactionStatus.FAILED
        } else {
            TransactionStatus.SUCCESS
        }

        // Detect transaction direction
        val direction = SmsBankPatterns.detectDirection(rawText)

        // Detect transaction type (UPI, card, etc.)
        val transactionType = SmsBankPatterns.detectTransactionType(rawText)

        // Extract merchant
        val merchant = SmsBankPatterns.extractMerchant(rawText)

        // Extract account info
        val accountInfo = SmsBankPatterns.extractAccountInfo(rawText)

        // Extract reference number
        val referenceNumber = SmsBankPatterns.extractReferenceNumber(rawText)

        return ParsedTransaction(
            amount = amount,
            currency = "INR",
            merchant = merchant,
            dateTime = System.currentTimeMillis(),
            transactionType = transactionType,
            direction = direction,
            referenceNumber = referenceNumber,
            accountInfo = accountInfo,
            status = status,
            sourceRaw = rawText,
            source = TransactionSource.SMS
        )
    }

    /**
     * Check if an SMS is likely a transaction message.
     * Quick pre-filter to avoid processing non-financial SMS.
     */
    fun isTransactionSms(body: String, sender: String): Boolean {
        // Check sender ID
        if (SmsBankPatterns.isKnownBankSender(sender)) {
            // Has amount pattern
            return SmsBankPatterns.AMOUNT_PATTERN.containsMatchIn(body)
        }

        // For unknown senders, check if the message has financial keywords + amount
        val hasFinancialKeywords = Regex(
            """(?:debited|credited|spent|paid|received|withdrawn|transferred|purchase|transaction|txn)""",
            RegexOption.IGNORE_CASE
        ).containsMatchIn(body)

        return hasFinancialKeywords && SmsBankPatterns.AMOUNT_PATTERN.containsMatchIn(body)
    }
}
