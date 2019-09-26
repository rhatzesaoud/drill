package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class WsMessage(
    val type: WsMessageType,
    val destination: String = "",
    val message: String = ""
)

enum class WsMessageType {
    MESSAGE, DELETE, UNAUTHORIZED, SUBSCRIBE, UNSUBSCRIBE
}