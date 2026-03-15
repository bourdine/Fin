package com.lottttto.miner.models

data class Transaction(
    val date: String,
    val amount: String,
    val time: String,
    val isIncoming: Boolean
)
