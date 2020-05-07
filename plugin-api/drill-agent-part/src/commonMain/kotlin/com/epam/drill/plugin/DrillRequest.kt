package com.epam.drill.plugin

import kotlinx.serialization.*

@Serializable
data class DrillRequest(
    val drillSessionId: String,
    val headers: Map<String, String>
)
