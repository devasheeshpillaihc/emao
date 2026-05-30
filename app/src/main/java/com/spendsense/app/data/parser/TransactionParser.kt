package com.spendsense.app.data.parser

import com.spendsense.app.domain.model.ParsedTransaction
import com.spendsense.app.domain.model.TransactionSource

/**
 * Interface for transaction parsing from various sources.
 * Implementations handle SMS, notifications, and email parsing.
 */
interface TransactionParser {
    /**
     * Attempt to parse a transaction from raw text.
     * @param rawText The raw SMS body, notification text, or email content
     * @param source The source type
     * @param sender Optional sender info (SMS sender ID, app package name)
     * @return ParsedTransaction if successfully parsed, null otherwise
     */
    fun parse(rawText: String, source: TransactionSource, sender: String = ""): ParsedTransaction?
}
