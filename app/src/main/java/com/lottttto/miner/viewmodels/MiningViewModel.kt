package com.lottttto.miner.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lottttto.miner.models.*
import com.lottttto.miner.repositories.MiningRepositoryImpl
import com.lottttto.miner.repositories.PoolRepositoryImpl
import com.lottttto.miner.repositories.WalletRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiningViewModel @Inject constructor(
    private val miningRepository: MiningRepositoryImpl,
    private val walletRepository: WalletRepositoryImpl,
    private val poolRepository: PoolRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiningUiState())
    val uiState: StateFlow<MiningUiState> = _uiState.asStateFlow()

    init {
        combine(
            miningRepository.miningStats,
            miningRepository.isMiningActive,
            walletRepository.getAllWallets(),
            flow { emit(poolRepository.getPoolsForCoin(CoinType.MONERO)) }
        ) { stats, isActive, wallets, pools ->
            _uiState.update { current ->
                current.copy(
                    currentCoin = stats.coin,
                    hashrate = stats.hashrate,
                    acceptedShares = stats.acceptedShares,
                    rejectedShares = stats.rejectedShares,
                    estimatedEarnings = stats.estimatedEarnings,
                    isMining = isActive,
                    allWallets = wallets,
                    allPools = pools
                )
            }
        }.launchIn(viewModelScope)
    }

    fun loadPoolsForCoin(coin: CoinType) {
        viewModelScope.launch {
            _uiState.update { it.copy(availablePools = poolRepository.getPoolsForCoin(coin)) }
        }
    }

    fun loadWalletsForCoin(coin: CoinType) {
        viewModelScope.launch {
            _uiState.update { it.copy(walletsForCoin = walletRepository.getWalletsForCoin(coin)) }
        }
    }

    fun selectMode(mode: MiningMode) { _uiState.update { it.copy(selectedMode = mode) } }
    fun selectPool(pool: Pool) { _uiState.update { it.copy(selectedPool = pool) } }
    fun selectWallet(wallet: Wallet) { _uiState.update { it.copy(selectedWallet = wallet) } }
    fun toggleWalletDropdown() { _uiState.update { it.copy(walletDropdownExpanded = !it.walletDropdownExpanded) } }
    fun togglePoolDropdown() { _uiState.update { it.copy(poolDropdownExpanded = !it.poolDropdownExpanded) } }

    fun startMining(coin: CoinType, mode: MiningMode, pool: Pool?, wallet: Wallet) {
        viewModelScope.launch { miningRepository.startMining(coin, mode, pool, wallet) }
    }

    fun stopMining() { viewModelScope.launch { miningRepository.stopMining() } }
}

data class MiningUiState(
    val selectedMode: MiningMode = MiningMode.POOL,
    val selectedPool: Pool? = null,
    val selectedWallet: Wallet? = null,
    val walletDropdownExpanded: Boolean = false,
    val poolDropdownExpanded: Boolean = false,
    val currentCoin: CoinType = CoinType.MONERO,
    val hashrate: Double = 0.0,
    val acceptedShares: Long = 0,
    val rejectedShares: Long = 0,
    val estimatedEarnings: Double = 0.0,
    val isMining: Boolean = false,
    val availablePools: List<Pool> = emptyList(),
    val walletsForCoin: List<Wallet> = emptyList(),
    val allWallets: List<Wallet> = emptyList(),
    val allPools: List<Pool> = emptyList()
)
