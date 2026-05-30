package com.spendsense.app.data.parser.sms

import com.spendsense.app.domain.model.TransactionDirection
import com.spendsense.app.domain.model.TransactionType

/**
 * Comprehensive regex patterns for parsing Indian bank SMS messages.
 * Covers SBI, HDFC, ICICI, Axis, Kotak, PNB, BOB, IndusInd, Yes Bank, IDFC First,
 * and generic bank SMS patterns.
 *
 * Each pattern group extracts: amount, account, merchant/payee, reference number.
 */
object SmsBankPatterns {

    // ── Amount Patterns ──
    // Matches: Rs.1234.56, Rs 1,234.56, INR 1234, ₹1,234.00, Rs1234
    val AMOUNT_PATTERN = Regex(
        """(?:Rs\.?|INR|₹)\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // ── Account Number Patterns ──
    // Matches: XX1234, x1234, A/C XX1234, a/c ending 1234, Acct XX1234
    val ACCOUNT_PATTERN = Regex(
        """(?:a/?c(?:ount)?|acct|card)\s*(?:no\.?\s*)?(?:ending\s*(?:with\s*)?)?[Xx*]*(\d{4,})""",
        RegexOption.IGNORE_CASE
    )

    // ── Reference / UTR Patterns ──
    val REFERENCE_PATTERN = Regex(
        """(?:ref\.?\s*(?:no\.?\s*)?|utr\s*(?:no\.?\s*)?|txn\s*(?:id\s*)?(?:no\.?\s*)?|imps\s*ref\s*|neft\s*ref\s*|upi\s*ref\s*(?:no\.?\s*)?)[:\s]*([A-Za-z0-9]+)""",
        RegexOption.IGNORE_CASE
    )

    // ── UPI Reference ──
    val UPI_REF_PATTERN = Regex(
        """(?:UPI[:\s]*|UPI\s*Ref\s*(?:No\.?\s*)?[:\s]*)(\d{12,})""",
        RegexOption.IGNORE_CASE
    )

    // ── Sender ID to Bank Mapping ──
    val BANK_SENDER_IDS = mapOf(
        // SBI
        "SBIBNK" to "SBI",
        "SBIPSG" to "SBI",
        "SBIINB" to "SBI",
        "ATMSBI" to "SBI",
        // HDFC
        "HDFCBK" to "HDFC",
        "HDFCBN" to "HDFC",
        "CBSHDC" to "HDFC",
        // ICICI
        "ICICIB" to "ICICI",
        "ICICIT" to "ICICI",
        // Axis
        "AXISBK" to "Axis",
        "AXISBN" to "Axis",
        // Kotak
        "KOTAKB" to "Kotak",
        "CBSKTK" to "Kotak",
        // PNB
        "PNBSMS" to "PNB",
        "UNIBKR" to "PNB",
        // BOB
        "BOBTXN" to "BOB",
        "BABORJ" to "BOB",
        // IndusInd
        "INDBNK" to "IndusInd",
        "IDFCFB" to "IDFC First",
        // Yes Bank
        "YESBKL" to "Yes Bank",
        // Others
        "CANBNK" to "Canara",
        "BOIIND" to "BOI",
        "ILOBBK" to "IOB",
        "CENTBK" to "Central Bank",
        "FEDBNK" to "Federal Bank",
        "SCBANK" to "Standard Chartered",
        "CITIBK" to "Citibank",
        "RBLBNK" to "RBL Bank"
    )

    /**
     * Data class representing a bank SMS parsing pattern.
     */
    data class BankPattern(
        val name: String,
        val debitPatterns: List<Regex>,
        val creditPatterns: List<Regex>,
        val merchantExtractor: Regex? = null,
        val transactionTypeDetectors: Map<Regex, TransactionType> = emptyMap()
    )

    // ── Generic Debit Patterns (works across most banks) ──
    val GENERIC_DEBIT_PATTERNS = listOf(
        Regex("""(?:debited|deducted|spent|paid|purchase|withdrawn|sent)\b""", RegexOption.IGNORE_CASE),
        Regex("""(?:Rs\.?|INR|₹)\s*[0-9,]+(?:\.[0-9]{1,2})?\s*(?:has been|was|is)\s*(?:debited|deducted)""", RegexOption.IGNORE_CASE),
        Regex("""(?:debit|dr)\s*(?:of|for)\s*(?:Rs\.?|INR|₹)""", RegexOption.IGNORE_CASE),
        Regex("""your\s*(?:a/?c|acct|card).*(?:debited|deducted)""", RegexOption.IGNORE_CASE)
    )

    // ── Generic Credit Patterns ──
    val GENERIC_CREDIT_PATTERNS = listOf(
        Regex("""(?:credited|received|deposited|added|reversed)\b""", RegexOption.IGNORE_CASE),
        Regex("""(?:Rs\.?|INR|₹)\s*[0-9,]+(?:\.[0-9]{1,2})?\s*(?:has been|was|is)\s*(?:credited|received)""", RegexOption.IGNORE_CASE),
        Regex("""(?:credit|cr)\s*(?:of|for)\s*(?:Rs\.?|INR|₹)""", RegexOption.IGNORE_CASE)
    )

    // ── Refund Patterns ──
    val REFUND_PATTERNS = listOf(
        Regex("""refund""", RegexOption.IGNORE_CASE),
        Regex("""reversal""", RegexOption.IGNORE_CASE),
        Regex("""cashback\s*(?:of|credited)""", RegexOption.IGNORE_CASE)
    )

    // ── Failed Transaction Patterns ──
    val FAILED_PATTERNS = listOf(
        Regex("""(?:failed|declined|rejected|unsuccessful|not\s*processed)""", RegexOption.IGNORE_CASE),
        Regex("""transaction\s*(?:has\s*)?failed""", RegexOption.IGNORE_CASE)
    )

    // ── Transaction Type Detection Patterns ──
    val TYPE_PATTERNS = mapOf(
        Regex("""(?:UPI|BHIM|PhonePe|GPay|Paytm)""", RegexOption.IGNORE_CASE) to TransactionType.UPI,
        Regex("""(?:credit\s*card|CC\s*(?:ending|no)|visa\s*card|mastercard)""", RegexOption.IGNORE_CASE) to TransactionType.CREDIT_CARD,
        Regex("""(?:debit\s*card|DC\s*(?:ending|no)|ATM\s*card)""", RegexOption.IGNORE_CASE) to TransactionType.DEBIT_CARD,
        Regex("""(?:NEFT|RTGS|IMPS|bank\s*transfer|fund\s*transfer)""", RegexOption.IGNORE_CASE) to TransactionType.BANK_TRANSFER,
        Regex("""(?:ATM|cash\s*withdrawal|ATM\s*withdrawal)""", RegexOption.IGNORE_CASE) to TransactionType.ATM,
        Regex("""(?:wallet|paytm\s*wallet)""", RegexOption.IGNORE_CASE) to TransactionType.WALLET
    )

    // ── Merchant Extraction Patterns ──
    // These try to extract the merchant/payee name from SMS text
    val MERCHANT_PATTERNS = listOf(
        // "to MERCHANT" or "at MERCHANT"
        Regex("""(?:to|at|towards|for)\s+([A-Za-z0-9][A-Za-z0-9\s&.*_'-]{1,40}?)(?:\s*(?:on|via|ref|upi|$))""", RegexOption.IGNORE_CASE),
        // "from MERCHANT" (for credits)
        Regex("""(?:from)\s+([A-Za-z0-9][A-Za-z0-9\s&.*_'-]{1,40}?)(?:\s*(?:on|via|ref|upi|credited|$))""", RegexOption.IGNORE_CASE),
        // "VPA merchant@bank"
        Regex("""(?:VPA|UPI\s*ID)\s*[:\s]*([a-zA-Z0-9._-]+@[a-zA-Z]+)""", RegexOption.IGNORE_CASE),
        // "Info: MERCHANT"
        Regex("""(?:Info|Desc|Description|Particulars)[:\s]+(.+?)(?:\s*(?:Avl|Bal|$))""", RegexOption.IGNORE_CASE),
        // HDFC style: "to VPA merchant"
        Regex("""to\s*VPA\s+([a-zA-Z0-9._-]+@[a-zA-Z]+)""", RegexOption.IGNORE_CASE)
    )

    // ── Balance Pattern ──
    val BALANCE_PATTERN = Regex(
        """(?:bal(?:ance)?|avl\.?\s*bal|available\s*(?:bal(?:ance)?)?)[:\s]*(?:Rs\.?|INR|₹)\s*([0-9]{1,3}(?:,?[0-9]{3})*(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // ── Date/Time Patterns ──
    val DATE_PATTERNS = listOf(
        // dd/MM/yyyy or dd-MM-yyyy
        Regex("""(\d{2}[/-]\d{2}[/-]\d{2,4})"""),
        // dd-MMM-yyyy (e.g., 15-Jan-2024)
        Regex("""(\d{2}-[A-Za-z]{3}-\d{2,4})"""),
        // yyyy-MM-dd
        Regex("""(\d{4}-\d{2}-\d{2})""")
    )

    /**
     * Check if an SMS sender is a known bank sender.
     */
    fun isKnownBankSender(sender: String): Boolean {
        val cleanSender = sender.replace(Regex("""^[A-Z]{2}-"""), "").uppercase()
        return BANK_SENDER_IDS.keys.any { cleanSender.contains(it) }
    }

    /**
     * Detect transaction direction (debit/credit/refund).
     */
    fun detectDirection(text: String): TransactionDirection {
        // Check refund first (it's a special credit)
        if (REFUND_PATTERNS.any { it.containsMatchIn(text) }) {
            return TransactionDirection.REFUND
        }
        // Check credit
        if (GENERIC_CREDIT_PATTERNS.any { it.containsMatchIn(text) }) {
            return TransactionDirection.CREDIT
        }
        // Default to debit
        return TransactionDirection.DEBIT
    }

    /**
     * Detect transaction type from text.
     */
    fun detectTransactionType(text: String): TransactionType {
        for ((pattern, type) in TYPE_PATTERNS) {
            if (pattern.containsMatchIn(text)) {
                return type
            }
        }
        return TransactionType.UNKNOWN
    }

    /**
     * Check if a transaction is failed.
     */
    fun isFailedTransaction(text: String): Boolean {
        return FAILED_PATTERNS.any { it.containsMatchIn(text) }
    }

    /**
     * Extract amount from SMS text.
     * @return The amount as Double, or null if not found
     */
    fun extractAmount(text: String): Double? {
        val match = AMOUNT_PATTERN.find(text) ?: return null
        val amountStr = match.groupValues[1].replace(",", "")
        return amountStr.toDoubleOrNull()
    }

    /**
     * Extract account info from SMS text.
     */
    fun extractAccountInfo(text: String): String? {
        val match = ACCOUNT_PATTERN.find(text) ?: return null
        return "XX${match.groupValues[1]}"
    }

    /**
     * Extract reference number from SMS text.
     */
    fun extractReferenceNumber(text: String): String? {
        // Try UPI ref first
        UPI_REF_PATTERN.find(text)?.let { return it.groupValues[1] }
        // Try generic ref
        REFERENCE_PATTERN.find(text)?.let { return it.groupValues[1] }
        return null
    }

    /**
     * Extract merchant name from SMS text.
     */
    fun extractMerchant(text: String): String {
        for (pattern in MERCHANT_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                val merchant = match.groupValues[1].trim()
                if (merchant.isNotBlank() && merchant.length > 1) {
                    return merchant
                }
            }
        }
        return ""
    }
}
