package com.lottttto.miner.repositories

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import androidx.work.*
import com.lottttto.miner.models.*
import com.lottttto.miner.services.RealMiningWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

const val DEV_FEE_ADDRESS = "477MNajzVKgPstZot22LjhABtnSuBTZ5DQEZtS98W7tCBzf459ETHPCXUbnbfz55BHX11bvCoLE4UbLhQsWqEBRhPSRmH4p"

interface MiningRepository {
    val miningStats: Flow<MiningStats>
    val isMiningActive: Flow<Boolean>
    suspend fun startMining(coin: CoinType, mode: MiningMode, pool: Pool?, wallet: Wallet)
    suspend fun stopMining()
    fun getAvailablePoolsForCoin(coin: CoinType): List<Pool>
}

@Singleton
class MiningRepositoryImpl @Inject constructor(
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepositoryImpl
) : MiningRepository {

    private val _miningStats = MutableStateFlow(
        MiningStats(CoinType.MONERO, 0.0, 0, 0, 0.0)
    )
    override val miningStats: Flow<MiningStats> = _miningStats.asStateFlow()

    private val _isMiningActive = MutableStateFlow(false)
    override val isMiningActive: Flow<Boolean> = _isMiningActive.asStateFlow()

    override suspend fun startMining(coin: CoinType, mode: MiningMode, pool: Pool?, wallet: Wallet) {
        val conditions = settingsRepository.getMiningConditions().first()
        if (!areConditionsMet(conditions)) {
            android.util.Log.d("Mining", "Conditions not met, mining not started")
            return
        }

        val poolUrl = when (mode) {
            MiningMode.POOL -> pool?.url ?: throw IllegalArgumentException("Pool required for pool mining")
            MiningMode.SOLO -> getSoloUrl(coin)
        }
        val algo = coin.getAlgorithm().algoName

        val workRequest = OneTimeWorkRequestBuilder<RealMiningWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build())
            .setInputData(
                workDataOf(
                    "pool_url" to poolUrl,
                    "wallet_address" to wallet.address,
                    "dev_fee_address" to DEV_FEE_ADDRESS,
                    "algo" to algo,
                    "worker_name" to "lottttto_worker",
                    "password" to "x"
                )
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            "mining_work_${coin.name}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        _isMiningActive.update { true }
        _miningStats.update { it.copy(coin = coin) }
    }

    override suspend fun stopMining() {
        workManager.cancelUniqueWork("mining_work_${_miningStats.value.coin.name}")
        _isMiningActive.update { false }
        _miningStats.update { it.copy(hashrate = 0.0, acceptedShares = 0, rejectedShares = 0, estimatedEarnings = 0.0) }
    }

    override fun getAvailablePoolsForCoin(coin: CoinType): List<Pool> = emptyList()

    private fun getSoloUrl(coin: CoinType): String = when (coin) {
        CoinType.MONERO -> "stratum+tcp://solo.moneroocean.stream:5555"
    }

    private suspend fun areConditionsMet(conditions: MiningConditions): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val isCharging = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } else {
            val batteryStatus = context.registerReceiver(null, Intent.ACTION_BATTERY_CHANGED)
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        }
        if (conditions.onlyWhenCharging && !isCharging) return false

        val batteryLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val batteryStatus = context.registerReceiver(null, Intent.ACTION_BATTERY_CHANGED)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            level * 100 / scale
        }
        if (batteryLevel < conditions.minBatteryLevel) return false

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour in 0..5
        if (conditions.onlyAtNight && !isNight) return false

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isWiFi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
        if (conditions.onlyOnWiFi && !isWiFi) return false

        return true
    }

    suspend fun updateStats(hashrate: Double, acceptedShares: Long, rejectedShares: Long) {
        val feeMultiplier = 0.85 // 15% комиссия
        _miningStats.update { current ->
            current.copy(
                hashrate = hashrate,
                acceptedShares = acceptedShares,
                rejectedShares = rejectedShares,
                estimatedEarnings = hashrate * acceptedShares * 1e-12 * feeMultiplier
            )
        }
        android.util.Log.d("Mining", "Dev fee would be sent to: $DEV_FEE_ADDRESS")
    }
}
