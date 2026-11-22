package com.example.security

import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.pinkoraPrefs: DataStore<Preferences> by preferencesDataStore(name = "pinkora_secure_prefs")

class SecurityManager(private val context: Context) {
    private val appContext = context.applicationContext

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val PIN_HASH = stringPreferencesKey("diary_pin_hash")
        val PIN_SALT = stringPreferencesKey("diary_pin_salt")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("diary_biometric_enabled")
    }

    val languageFlow: Flow<AppLanguage> = appContext.pinkoraPrefs.data.map { prefs ->
        AppLanguage.fromCode(prefs[Keys.LANGUAGE])
    }

    val hasPinFlow: Flow<Boolean> = appContext.pinkoraPrefs.data.map { prefs ->
        !prefs[Keys.PIN_HASH].isNullOrBlank() && !prefs[Keys.PIN_SALT].isNullOrBlank()
    }

    val biometricEnabledFlow: Flow<Boolean> = appContext.pinkoraPrefs.data.map { prefs ->
        prefs[Keys.BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setLanguage(language: AppLanguage) {
        appContext.pinkoraPrefs.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setPin(pin: String) {
        require(pin.length == 4 && pin.all { it.isDigit() }) { "PIN must be 4 digits" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2Hash(pin, salt)
        appContext.pinkoraPrefs.edit { prefs ->
            prefs[Keys.PIN_SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
            prefs[Keys.PIN_HASH] = Base64.encodeToString(hash, Base64.NO_WRAP)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = appContext.pinkoraPrefs.data.first()
        val saltB64 = prefs[Keys.PIN_SALT] ?: return false
        val hashB64 = prefs[Keys.PIN_HASH] ?: return false
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val expectedHash = Base64.decode(hashB64, Base64.NO_WRAP)
        val candidateHash = pbkdf2Hash(pin, salt)
        return MessageDigest.isEqual(expectedHash, candidateHash)
    }

    suspend fun clearPin() {
        appContext.pinkoraPrefs.edit { prefs ->
            prefs.remove(Keys.PIN_HASH)
            prefs.remove(Keys.PIN_SALT)
            prefs[Keys.BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        appContext.pinkoraPrefs.edit { prefs ->
            prefs[Keys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun clearAllLocalSecurity() {
        appContext.pinkoraPrefs.edit { it.clear() }
    }

    fun isBiometricAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val manager = appContext.getSystemService(BiometricManager::class.java) ?: return false
        @Suppress("DEPRECATION")
        return manager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticateWithBiometrics(
        activityContext: Context,
        title: String,
        subtitle: String,
        negativeText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            onError("Biometric unlock requires Android 9 or newer.")
            return
        }
        val prompt = BiometricPrompt.Builder(activityContext)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButton(negativeText, activityContext.mainExecutor) { _, _ -> }
            .build()
        prompt.authenticate(
            CancellationSignal(),
            activityContext.mainExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    onError(errString?.toString() ?: "Biometric authentication unavailable.")
                }
            },
        )
    }

    private fun pbkdf2Hash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 65536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
