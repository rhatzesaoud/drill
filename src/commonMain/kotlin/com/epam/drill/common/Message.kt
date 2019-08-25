package com.epam.drill.common

import kotlinx.serialization.Serializable

@Serializable
data class Message(var type: MessageType, var destination: String = "", var message: String = "")

@Serializable
data class PluginMessage(
        val event: DrillEvent,
        val pluginFile: List<Byte> = emptyList(),
        val nativePart: NativePlugin? = null,
        val pl: PluginBean
)

@Serializable
data class NativePlugin(
        val windowsPlugin: List<Byte> = emptyList(),
        val linuxPluginFileBytes: List<Byte> = emptyList()
)

