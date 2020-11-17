package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class BuildInfo(
    val version: String = ""
) {
    override fun equals(other: Any?) = other is BuildInfo && version == other.version

    override fun hashCode() = version.hashCode()
}

@Serializable
data class ByteClass(
    val className: String,
    val bytes: ByteArray
)
