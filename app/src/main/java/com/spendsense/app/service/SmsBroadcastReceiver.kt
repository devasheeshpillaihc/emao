package com.spendsense.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.spendsense.app.data.source.SmsReader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/**
 * BroadcastReceiver for incoming SMS messages.
 * Parses financial SMS in real-time as they arrive.
 */
@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsReader: SmsReader

    companion object {
        private const val TAG = "SmsBroadcastReceiver"

        private val _smsTransactionFlow = MutableSharedFlow<SmsTransactionEvent>(
            extraBufferCapacity = 50
        )
        val smsTransactionFlow: SharedFlow<SmsTransactionEvent> = _smsTransactionFlow
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Group message parts by sender (multi-part SMS)
        val groupedMessages = messages.groupBy { it.displayOriginatingAddress ?: "" }

        for ((sender, parts) in groupedMessages) {
            val fullBody = parts.joinToString("") { it.displayMessageBody ?: "" }
            val timestamp = parts.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

            Log.d(TAG, "Received SMS from $sender: ${fullBody.take(50)}...")

            // Parse immediately
            val parsed = smsReader.parseSingleSms(sender, fullBody, timestamp)
            if (parsed != null) {
                Log.d(TAG, "Parsed transaction: ₹${parsed.amount}")
                CoroutineScope(Dispatchers.Default).launch {
                    _smsTransactionFlow.emit(
                        SmsTransactionEvent(
                            parsedTransaction = parsed,
                            sender = sender,
                            timestamp = timestamp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Event wrapper for SMS-parsed transactions.
 */
data class SmsTransactionEvent(
    val parsedTransaction: com.spendsense.app.domain.model.ParsedTransaction,
    val sender: String,
    val timestamp: Long
)
