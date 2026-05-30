package com.spendsense.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * SpendSense Application class.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class SpendSenseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide configurations here
    }
}
