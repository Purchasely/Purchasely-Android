package com.purchasely.samplev2.domain.log

data class LogMessage(
    val message: String,
    val level: String,
    val date: String,
)