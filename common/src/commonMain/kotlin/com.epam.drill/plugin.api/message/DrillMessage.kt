package com.epam.drill.plugin.api.message

import kotlinx.serialization.*

@Serializable
data class DrillMessage(val content: String)
