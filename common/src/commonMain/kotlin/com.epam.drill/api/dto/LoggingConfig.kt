package com.epam.drill.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoggingConfig(
    val warn: Boolean = false,
    val info: Boolean = false,
    val debug: Boolean = false,
    val trace: Boolean = false,
    val error: Boolean = false
)