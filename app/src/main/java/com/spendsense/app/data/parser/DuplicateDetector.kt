package com.spendsense.app.data.parser

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates fingerprints for transactions to detect and prevent duplicates.
 *
 * Fingerprinting strategy:
 * - Uses amount + normalized merchant + timestamp window + reference number
 * - Timestamp window: rounds to nearest 5-minute block to handle slight time differences
 *   between SMS and notification for the same transaction
 */
@Singleton
class DuplicateDetector @Inject constructor() {

    companion object {
        // 5-minute window in milliseconds
        private const val TIMESTAMP_WINDOW_MS = 5 * 60 * 1000L
    }

    /**
     * Generate a fingerprint for a transaction.
     * Transactions with the same fingerprint are considered duplicates.
     *
     * @param amount Transaction amount
     * @param merchant Normalized merchant name (lowercased, trimmed)
     * @param timestamp Transaction timestamp in epoch millis
     * @param referenceNumber Optional reference/UPI transaction ID
     * @return SHA-256 hash fingerprint string
     */
    fun generateFingerprint(
        amount: Double,
        merchant: String,
        timestamp: Long,
        referenceNumber: String? = null
    ): String {
        // If we have a reference number, use it as the primary identifier
        // since it's unique per transaction
        if (!referenceNumber.isNullOrBlank()) {
            return hash("ref:${referenceNumber.trim().lowercase()}")
        }

        // Otherwise, use amount + merchant + time window
        val timeWindow = timestamp / TIMESTAMP_WINDOW_MS
        val normalizedMerchant = merchant.trim().lowercase()
        val amountStr = String.format("%.2f", amount)

        return hash("$amountStr|$normalizedMerchant|$timeWindow")
    }

    /**
     * Check if two transactions might be duplicates.
     * More lenient check used when fingerprints don't match
     * but we still want to verify.
     */
    fun arePotentialDuplicates(
        amount1: Double,
        merchant1: String,
        timestamp1: Long,
        amount2: Double,
        merchant2: String,
        timestamp2: Long
    ): Boolean {
        // Same amount
        if (Math.abs(amount1 - amount2) > 0.01) return false

        // Within time window
        if (Math.abs(timestamp1 - timestamp2) > TIMESTAMP_WINDOW_MS) return false

        // Same or similar merchant
        val m1 = merchant1.trim().lowercase()
        val m2 = merchant2.trim().lowercase()
        if (m1 == m2) return true
        if (m1.isBlank() || m2.isBlank()) return true // If one is blank, could be same
        if (m1.contains(m2) || m2.contains(m1)) return true

        return false
    }

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
