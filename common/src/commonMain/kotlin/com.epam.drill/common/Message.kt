package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class Message(
    val type: MessageType,
    val destination: String = "",
    val data: ByteArray = byteArrayOf()
)
