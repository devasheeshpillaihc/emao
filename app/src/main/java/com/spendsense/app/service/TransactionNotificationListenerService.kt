package com.spendsense.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.spendsense.app.data.parser.notification.NotificationParser
import com.spendsense.app.domain.model.TransactionSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/**
 * Android NotificationListenerService that monitors transaction notifications
 * from UPI apps (Google Pay, PhonePe, Paytm) and bank apps.
 *
 * Requires user to grant Notification Access permission in device settings.
 */
@AndroidEntryPoint
class TransactionNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationParser: NotificationParser

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        private const val TAG = "TxnNotifListener"

        // Shared flow to emit parsed transactions to the repository
        private val _transactionFlow = MutableSharedFlow<NotificationTransactionEvent>(
            extraBufferCapacity = 50
        )
        val transactionFlow: SharedFlow<NotificationTransactionEvent> = _transactionFlow
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val packageName = sbn.packageName ?: return

        // Only process notifications from monitored financial apps
        if (!notificationParser.shouldMonitor(packageName)) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Extract notification text
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        // Use the most detailed text available
        val fullText = when {
            bigText.isNotBlank() -> "$title $bigText"
            text.isNotBlank() -> "$title $text"
            else -> return
        }

        Log.d(TAG, "Processing notification from $packageName: $fullText")

        // Parse the notification
        serviceScope.launch {
            try {
                val parsed = notificationParser.parse(fullText, TransactionSource.NOTIFICATION, packageName)
                if (parsed != null) {
                    Log.d(TAG, "Parsed transaction: ₹${parsed.amount} to ${parsed.merchant}")
                    _transactionFlow.emit(
                        NotificationTransactionEvent(
                            parsedTransaction = parsed,
                            packageName = packageName,
                            timestamp = sbn.postTime
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // No action needed on removal
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

/**
 * Event wrapper for parsed notification transactions.
 */
data class NotificationTransactionEvent(
    val parsedTransaction: com.spendsense.app.domain.model.ParsedTransaction,
    val packageName: String,
    val timestamp: Long
)
