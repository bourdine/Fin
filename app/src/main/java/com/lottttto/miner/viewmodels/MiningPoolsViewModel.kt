package com.lottttto.miner.viewmodels

import androidx.lifecycle.ViewModel
import com.lottttto.miner.models.CoinType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MiningPoolsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MiningPoolsUiState())
    val uiState: StateFlow<MiningPoolsUiState> = _uiState
}

data class MiningPoolsUiState(
    val pools: Map<CoinType, String> = emptyMap()
)
