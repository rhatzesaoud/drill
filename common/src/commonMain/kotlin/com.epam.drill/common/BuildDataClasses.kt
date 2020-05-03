package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class Method(
    val ownerClass: String,
    val name: String,
    val desc: String,
    val hash: String?
)

typealias Methods = List<Method>

@Serializable
data class MethodChanges(val map: Map<DiffType, Methods> = emptyMap())

@Serializable
data class BuildInfo(
    val version: String = "",
    val parentVersion: String = "",
    val methodChanges: MethodChanges = MethodChanges(),
    val classesBytes: Map<String, ByteArray> = emptyMap(),
    val javaMethods: Map<String, Methods> = emptyMap()
) {
    override fun equals(other: Any?) = other is BuildInfo && version == other.version

    override fun hashCode() = version.hashCode()
}

@Serializable
data class ByteClass(
    val className: String,
    val bytes: ByteArray
)

enum class DiffType {
    MODIFIED_NAME,
    MODIFIED_DESC,
    MODIFIED_BODY,
    NEW,
    DELETED,
    UNAFFECTED
}
