package com.lottttto.miner.models

enum class CoinType {
    MONERO
}

fun CoinType.getDisplayName(): String = when (this) {
    CoinType.MONERO -> "Monero (XMR)"
}

enum class MiningMode { SOLO, POOL }

enum class MiningAlgorithm(val algoName: String) {
    RANDOM_X("rx/0")
}

fun CoinType.getAlgorithm(): MiningAlgorithm = when (this) {
    CoinType.MONERO -> MiningAlgorithm.RANDOM_X
}
