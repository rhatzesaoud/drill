package com.epam.drill.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    val parameters: Map<String, String>
)
