package com.lottttto.miner.repositories

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

interface PinRepository {
    suspend fun isFirstLaunch(): Boolean
    suspend fun setPin(pinHash: String)
    suspend fun checkPin(pinHash: String): Boolean
    suspend fun hasPin(): Boolean
    suspend fun clearPin()
}

@Singleton
class PinRepositoryImpl @Inject constructor(
    context: Context
) : PinRepository {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        "pin_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun isFirstLaunch(): Boolean = withContext(Dispatchers.IO) {
        !prefs.contains("pin_hash")
    }

    override suspend fun setPin(pinHash: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString("pin_hash", pinHash).apply()
    }

    override suspend fun checkPin(pinHash: String): Boolean = withContext(Dispatchers.IO) {
        prefs.getString("pin_hash", null) == pinHash
    }

    override suspend fun hasPin(): Boolean = withContext(Dispatchers.IO) {
        prefs.contains("pin_hash")
    }

    override suspend fun clearPin() = withContext(Dispatchers.IO) {
        prefs.edit().remove("pin_hash").apply()
    }

    companion object {
        fun hashPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
