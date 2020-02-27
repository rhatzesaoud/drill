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

data class BuildInfo(
    val version: String = "",
    val parentVersion: String = "",
    val methodChanges: MethodChanges = MethodChanges(),
    val classesBytes: Map<String, ByteArray> = emptyMap(),
    val javaMethods: Map<String, Methods> = emptyMap(),
    val new: Boolean = true
) {
    override fun equals(other: Any?) = other is BuildInfo && version == other.version

    override fun hashCode() = version.hashCode()

    @Deprecated(message = "Old inconsistent name", replaceWith = ReplaceWith("version"))
    val buildVersion get() = version

    @Deprecated(message = "Old inconsistent name", replaceWith = ReplaceWith("parentVersion"))
    val prevBuild get() = parentVersion
}

@Serializable
data class Base64Class(
    val className: String,
    val encodedBytes: String
)

enum class DiffType {
    MODIFIED_NAME,
    MODIFIED_DESC,
    MODIFIED_BODY,
    NEW,
    DELETED,
    UNAFFECTED
}
