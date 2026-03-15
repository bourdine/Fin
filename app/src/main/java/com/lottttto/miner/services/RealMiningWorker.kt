package com.lottttto.miner.services

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lottttto.miner.repositories.DEV_FEE_ADDRESS
import com.lottttto.miner.utils.NativeMinerLib
import kotlinx.coroutines.delay

class RealMiningWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val nativeMiner = NativeMinerLib()

    override suspend fun doWork(): Result {
        val poolUrl = inputData.getString("pool_url") ?: return Result.failure()
        val walletAddress = inputData.getString("wallet_address") ?: return Result.failure()
        val devFeeAddress = inputData.getString("dev_fee_address") ?: DEV_FEE_ADDRESS
        val algo = inputData.getString("algo") ?: return Result.failure()
        val workerName = inputData.getString("worker_name") ?: "worker1"
        val password = inputData.getString("password") ?: "x"

        Log.d("MiningWorker", "Starting mining for wallet: $walletAddress")
        Log.d("MiningWorker", "Dev fee will be sent to: $devFeeAddress")

        val started = nativeMiner.startMining(poolUrl, walletAddress, workerName, password, algo, 1)
        if (!started) return Result.failure()

        while (!isStopped) delay(5000)

        nativeMiner.stopMining()
        return Result.success()
    }
}
