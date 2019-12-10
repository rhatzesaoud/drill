package com.epam.drill.common.ws

import kotlinx.serialization.*

@Serializable
data class ServiceConfig(val sslPort: String)