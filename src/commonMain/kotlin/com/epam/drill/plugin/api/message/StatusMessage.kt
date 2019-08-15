package com.epam.drill.plugin.api.message

import kotlinx.serialization.Serializable

@Serializable
open class StatusMessage(val code: Int, val message: String)