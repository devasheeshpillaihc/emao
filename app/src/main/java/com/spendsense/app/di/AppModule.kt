package com.spendsense.app.di

import android.content.Context
import com.spendsense.app.util.security.AppLockManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for app-wide utility providers.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppLockManager(@ApplicationContext context: Context): AppLockManager {
        return AppLockManager(context)
    }
}
