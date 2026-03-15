package com.lottttto.miner.repositories

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.lottttto.miner.models.MiningConditions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

interface SettingsRepository {
    suspend fun saveComputingUsage(usage: Int)
    fun getComputingUsage(): Flow<Int>
    suspend fun saveTaskWeights(weights: List<Int>)
    fun getTaskWeights(): Flow<List<Int>>
    suspend fun saveMiningConditions(conditions: MiningConditions)
    fun getMiningConditions(): Flow<MiningConditions>
    suspend fun saveTotalPower(power: Int)
    fun getTotalPower(): Flow<Int>
    suspend fun savePoolPercent(percent: Int)
    fun getPoolPercent(): Flow<Int>
    suspend fun saveSoloPercent(percent: Int)
    fun getSoloPercent(): Flow<Int>
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val context: Context
) : SettingsRepository {
    private val dataStore = context.dataStore
    private val gson = Gson()

    companion object {
        val COMPUTING_USAGE = intPreferencesKey("computing_usage")
        val TASK_WEIGHTS = stringPreferencesKey("task_weights")
        val MINING_CONDITIONS = stringPreferencesKey("mining_conditions")
        val TOTAL_POWER = intPreferencesKey("total_power")
        val POOL_PERCENT = intPreferencesKey("pool_percent")
        val SOLO_PERCENT = intPreferencesKey("solo_percent")
    }

    override suspend fun saveComputingUsage(usage: Int) {
        dataStore.edit { prefs -> prefs[COMPUTING_USAGE] = usage }
    }

    override fun getComputingUsage(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[COMPUTING_USAGE] ?: 15
    }

    override suspend fun saveTaskWeights(weights: List<Int>) {
        val json = weights.joinToString(",")
        dataStore.edit { prefs -> prefs[TASK_WEIGHTS] = json }
    }

    override fun getTaskWeights(): Flow<List<Int>> = dataStore.data.map { prefs ->
        prefs[TASK_WEIGHTS]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: defaultTaskWeights()
    }

    private fun defaultTaskWeights(): List<Int> = listOf(50, 50)

    override suspend fun saveMiningConditions(conditions: MiningConditions) {
        val json = gson.toJson(conditions)
        dataStore.edit { prefs -> prefs[MINING_CONDITIONS] = json }
    }

    override fun getMiningConditions(): Flow<MiningConditions> = dataStore.data.map { prefs ->
        val json = prefs[MINING_CONDITIONS]
        if (json != null) {
            gson.fromJson(json, MiningConditions::class.java)
        } else {
            MiningConditions()
        }
    }

    override suspend fun saveTotalPower(power: Int) {
        dataStore.edit { prefs -> prefs[TOTAL_POWER] = power }
    }

    override fun getTotalPower(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[TOTAL_POWER] ?: 15
    }

    override suspend fun savePoolPercent(percent: Int) {
        dataStore.edit { prefs -> prefs[POOL_PERCENT] = percent }
    }

    override fun getPoolPercent(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[POOL_PERCENT] ?: 50
    }

    override suspend fun saveSoloPercent(percent: Int) {
        data
