package com.spendsense.app.util.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_lock_prefs")

/**
 * Manages app lock via PIN with secure hashed storage.
 */
@Singleton
class AppLockManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val PIN_HASH_KEY = stringPreferencesKey("pin_hash")
        private val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        private val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_PIN_ENABLED] ?: false
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_BIOMETRIC_ENABLED] ?: false
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DARK_MODE] ?: true
    }

    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        context.dataStore.edit { prefs ->
            prefs[PIN_HASH_KEY] = hash
            prefs[IS_PIN_ENABLED] = true
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        var storedHash = ""
        context.dataStore.data.collect { prefs ->
            storedHash = prefs[PIN_HASH_KEY] ?: ""
            return@collect
        }
        return hashPin(pin) == storedHash
    }

    suspend fun removePin() {
        context.dataStore.edit { prefs ->
            prefs.remove(PIN_HASH_KEY)
            prefs[IS_PIN_ENABLED] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_MODE] = enabled
        }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
