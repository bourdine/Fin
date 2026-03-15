package com.lottttto.miner.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lottttto.miner.models.MiningConditions
import com.lottttto.miner.repositories.SettingsRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoneroMiningSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoneroMiningSettingsUiState())
    val uiState: StateFlow<MoneroMiningSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getMiningConditions().collect { conditions ->
                _uiState.update { it.copy(
                    onlyWhenCharging = conditions.onlyWhenCharging,
                    onlyAtNight = conditions.onlyAtNight,
                    onlyOnWiFi = conditions.onlyOnWiFi,
                    minBatteryLevel = conditions.minBatteryLevel,
                    stopOnLowBattery = conditions.stopOnLowBattery,
                    stopOnOverheat = conditions.stopOnOverheat,
                    autoStartOnLaunch = conditions.autoStartOnLaunch
                )}
            }
            settingsRepository.getTotalPower().collect { power ->
                _uiState.update { it.copy(totalPower = power) }
            }
            settingsRepository.getPoolPercent().collect { percent ->
                _uiState.update { it.copy(poolPercent = percent) }
            }
            settingsRepository.getSoloPercent().collect { percent ->
                _uiState.update { it.copy(soloPercent = percent) }
            }
        }
    }

    fun setMiningMode(mode: String) {
        _uiState.update { it.copy(miningMode = mode) }
    }

    fun setSelectedPool(pool: String) {
        _uiState.update { it.copy(selectedPool = pool) }
    }

    fun setTotalPower(power: Int) {
        _uiState.update { it.copy(totalPower = power) }
        viewModelScope.launch {
            settingsRepository.saveTotalPower(power)
        }
    }

    fun setPoolPercent(percent: Int) {
        _uiState.update { it.copy(poolPercent = percent) }
        viewModelScope.launch {
            settingsRepository.savePoolPercent(percent)
        }
    }

    fun setSoloPercent(percent: Int) {
        _uiState.update { it.copy(soloPercent = percent) }
        viewModelScope.launch {
            settingsRepository.saveSoloPercent(percent)
        }
    }

    fun setOnlyWhenCharging(value: Boolean) {
        _uiState.update { it.copy(onlyWhenCharging = value) }
        saveConditions()
    }

    fun setOnlyAtNight(value: Boolean) {
        _uiState.update { it.copy(onlyAtNight = value) }
        saveConditions()
    }

    fun setOnlyOnWiFi(value: Boolean) {
        _uiState.update { it.copy(onlyOnWiFi = value) }
        saveConditions()
    }

    fun setMinBatteryLevel(value: Int) {
        _uiState.update { it.copy(minBatteryLevel = value) }
        saveConditions()
    }

    fun setStopOnLowBattery(value: Boolean) {
        _uiState.update { it.copy(stopOnLowBattery = value) }
        saveConditions()
    }

    fun setStopOnOverheat(value: Boolean) {
        _uiState.update { it.copy(stopOnOverheat = value) }
        saveConditions()
    }

    fun setAutoStartOnLaunch(value: Boolean) {
        _uiState.update { it.copy(autoStartOnLaunch = value) }
        saveConditions()
    }

    private fun saveConditions() {
        viewModelScope.launch {
            settingsRepository.saveMiningConditions(uiState.value.toConditions())
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            settingsRepository.saveTotalPower(uiState.value.totalPower)
            settingsRepository.savePoolPercent(uiState.value.poolPercent)
            settingsRepository.saveSoloPercent(uiState.value.soloPercent)
            settingsRepository.saveMiningConditions(uiState.value.toConditions())
        }
    }

    private fun MoneroMiningSettingsUiState.toConditions() = MiningConditions(
        onlyWhenCharging = onlyWhenCharging,
        onlyAtNight = onlyAtNight,
        onlyOnWiFi = onlyOnWiFi,
        minBatteryLevel = minBatteryLevel,
        stopOnLowBattery = stopOnLowBattery,
        stopOnOverheat = stopOnOverheat,
        autoStartOnLaunch = autoStartOnLaunch
    )
}

data class MoneroMiningSettingsUiState(
    val miningMode: String = "both",
    val selectedPool: String = "MoneroOcean",
    val totalPower: Int = 15,
    val poolPercent: Int = 50,
    val soloPercent: Int = 50,
    val onlyWhenCharging: Boolean = false,
    val onlyAtNight: Boolean = false,
    val onlyOnWiFi: Boolean = true,
    val minBatteryLevel: Int = 20,
    val stopOnLowBattery: Boolean = true,
    val stopOnOverheat: Boolean = true,
    val autoStartOnLaunch: Boolean = true
)
