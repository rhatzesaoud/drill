package com.epam.drill.plugin.api.end

data class ActionResult(
    val code: Int,
    val data: Any,
    val agentAction: Any? = null
)
