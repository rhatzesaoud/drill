package com.epam.drill.plugin.api.message

import kotlinx.serialization.*

@Serializable
data class MessageWrapper(val pluginId: String, val drillMessage: DrillMessage)
