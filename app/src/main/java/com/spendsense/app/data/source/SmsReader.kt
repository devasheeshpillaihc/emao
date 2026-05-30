package com.spendsense.app.data.source

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.spendsense.app.data.parser.sms.SmsBankPatterns
import com.spendsense.app.data.parser.sms.SmsTransactionParser
import com.spendsense.app.domain.model.ParsedTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads SMS messages from the device's SMS inbox using ContentResolver.
 * Filters for bank/financial SMS and parses them for transaction data.
 */
@Singleton
class SmsReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsParser: SmsTransactionParser
) {

    companion object {
        private const val SMS_URI = "content://sms/inbox"
        private const val DEFAULT_DAYS_TO_SCAN = 30
        private const val MAX_SMS_TO_SCAN = 5000
    }

    /**
     * Scan SMS inbox for transaction messages.
     *
     * @param daysBack Number of days to scan back from now
     * @return List of successfully parsed transactions
     */
    suspend fun scanSmsInbox(daysBack: Int = DEFAULT_DAYS_TO_SCAN): List<ParsedTransaction> =
        withContext(Dispatchers.IO) {
            val transactions = mutableListOf<ParsedTransaction>()

            val cutoffTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)

            val cursor: Cursor? = context.contentResolver.query(
                Uri.parse(SMS_URI),
                arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                "${Telephony.Sms.DATE} > ? AND ${Telephony.Sms.TYPE} = ?",
                arrayOf(cutoffTime.toString(), Telephony.Sms.MESSAGE_TYPE_INBOX.toString()),
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                var count = 0
                while (it.moveToNext() && count < MAX_SMS_TO_SCAN) {
                    val sender = it.getString(addressIdx) ?: continue
                    val body = it.getString(bodyIdx) ?: continue
                    val date = it.getLong(dateIdx)

                    count++

                    // Pre-filter: only process SMS from known bank senders or with financial keywords
                    if (!smsParser.isTransactionSms(body, sender)) continue

                    // Parse the SMS
                    val parsed = smsParser.parse(body, com.spendsense.app.domain.model.TransactionSource.SMS, sender)
                    if (parsed != null) {
                        // Use the SMS date instead of current time
                        transactions.add(parsed.copy(dateTime = date))
                    }
                }
            }

            transactions
        }

    /**
     * Parse a single SMS message (used by the broadcast receiver for real-time detection).
     */
    fun parseSingleSms(sender: String, body: String, timestamp: Long): ParsedTransaction? {
        if (!smsParser.isTransactionSms(body, sender)) return null
        val parsed = smsParser.parse(body, com.spendsense.app.domain.model.TransactionSource.SMS, sender)
        return parsed?.copy(dateTime = timestamp)
    }
}
