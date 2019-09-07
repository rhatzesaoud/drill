package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class AgentConfig(
        val id: String,
        var buildVersion: String,
        var needSync: Boolean = true

)

const val AgentConfigParam = "AgentConfig"
const val NeedSyncParam = "needSync"