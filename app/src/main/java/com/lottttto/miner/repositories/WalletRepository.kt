package com.lottttto.miner.repositories

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lottttto.miner.models.CoinType
import com.lottttto.miner.models.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface WalletRepository {
    fun getAllWallets(): Flow<List<Wallet>>
    suspend fun getWalletsForCoin(coin: CoinType): List<Wallet>
    suspend fun addWallet(address: String, coin: CoinType, label: String?, seedPhrase: String?)
    suspend fun updateWallet(wallet: Wallet)
    suspend fun deleteWallet(id: Long)
}

@Singleton
class WalletRepositoryImpl @Inject constructor(
    context: Context
) : WalletRepository {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        "wallet_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val gson = Gson()
    private val walletListType = object : TypeToken<List<Wallet>>() {}.type

    override fun getAllWallets(): Flow<List<Wallet>> = flow {
        val json = prefs.getString("wallets", "[]")
        val list: List<Wallet> = gson.fromJson(json, walletListType)
        emit(list)
    }

    override suspend fun getWalletsForCoin(coin: CoinType): List<Wallet> = withContext(Dispatchers.IO) {
        val json = prefs.getString("wallets", "[]")
        val list: List<Wallet> = gson.fromJson(json, walletListType)
        list.filter { it.coin == coin }
    }

    override suspend fun addWallet(address: String, coin: CoinType, label: String?, seedPhrase: String?) = withContext(Dispatchers.IO) {
        val current = getAllWalletsSync()
        val newWallet = Wallet(
            id = (current.maxOfOrNull { it.id } ?: 0) + 1,
            address = address,
            coin = coin,
            label = label,
            seedPhrase = seedPhrase
        )
        saveWallets(current + newWallet)
    }

    override suspend fun updateWallet(wallet: Wallet) = withContext(Dispatchers.IO) {
        val current = getAllWalletsSync()
        val updated = current.map { if (it.id == wallet.id) wallet else it }
        saveWallets(updated)
    }

    override suspend fun deleteWallet(id: Long) = withContext(Dispatchers.IO) {
        val current = getAllWalletsSync()
        val updated = current.filter { it.id != id }
        saveWallets(updated)
    }

    private fun getAllWalletsSync(): List<Wallet> {
        val json = prefs.getString("wallets", "[]")
        return gson.fromJson(json, walletListType)
    }

    private fun saveWallets(wallets: List<Wallet>) {
        val json = gson.toJson(wallets)
        prefs.edit().putString("wallets", json).apply()
    }
}
