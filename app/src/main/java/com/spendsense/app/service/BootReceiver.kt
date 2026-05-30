package com.spendsense.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver that fires after device boot to reschedule recurring transaction checks.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted — recurring transaction scheduler can be re-initialized")
            // Future: Re-schedule recurring transaction alarms via WorkManager or AlarmManager
        }
    }
}
