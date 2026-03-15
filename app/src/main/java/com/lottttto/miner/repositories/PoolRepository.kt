package com.lottttto.miner.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lottttto.miner.models.CoinType
import com.lottttto.miner.models.Pool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface PoolRepository {
    suspend fun getPoolsForCoin(coin: CoinType): List<Pool>
}

@Singleton
class PoolRepositoryImpl @Inject constructor(
    private val context: Context
) : PoolRepository {
    private val gson = Gson()
    private val poolListType = object : TypeToken<List<Pool>>() {}.type

    override suspend fun getPoolsForCoin(coin: CoinType): List<Pool> = withContext(Dispatchers.IO) {
        val json = context.assets.open("pools.json").bufferedReader().use { it.readText() }
        val list: List<Pool> = gson.fromJson(json, poolListType)
        list.filter { it.coin == coin }
    }
}
