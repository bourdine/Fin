package com.lottttto.miner.models

data class MiningConditions(
    val onlyWhenCharging: Boolean = false,
    val onlyAtNight: Boolean = false,
    val onlyOnWiFi: Boolean = false,
    val minBatteryLevel: Int = 20,
    val stopOnLowBattery: Boolean = true,
    val stopOnOverheat: Boolean = true,
    val autoStartOnLaunch: Boolean = true
)
