package com.lottttto.miner.utils

import android.util.Log

class NativeMinerLib {
    companion object {
        init {
            try {
                System.loadLibrary("miner_jni")
                Log.d("NativeMinerLib", "Native library loaded")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("NativeMinerLib", "Failed to load native library", e)
            }
        }
    }

    external fun startMining(poolUrl: String, walletAddress: String, workerName: String, password: String, algo: String, threads: Int): Boolean
    external fun stopMining(): Boolean
    external fun getHashrate(): Double
    external fun getAcceptedShares(): Long
    external fun getRejectedShares(): Long
}
