package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class Method(
    val ownerClass: String,
    val name: String,
    val desc: String,
    val hash: String?
) {

    val sign = "$name$desc"

    fun nameModified(otherMethod: Method) = hash == otherMethod.hash && desc == otherMethod.desc

    fun descriptorModified(otherMethod: Method) = name == otherMethod.name && hash == otherMethod.hash

    fun bodyModified(otherMethod: Method) = name == otherMethod.name && desc == otherMethod.desc

}

typealias Methods = List<Method>

@Serializable
data class MethodChanges(val map: Map<DiffType, List<Method>> = emptyMap()){
    val notEmpty: Boolean
        get() = map.keys
    .filter { it != DiffType.UNAFFECTED }
    .mapNotNull { map[it] }
    .flatten()
    .isNotEmpty()
}

data class BuildInfo(
    val buildVersion: String = "",
    val buildAlias: String = "",
    val buildSummary: BuildSummary = BuildSummary(),
    val prevBuild: String = "",
    val methodChanges: MethodChanges = MethodChanges(),
    val classesBytes: Map<String, ByteArray> = emptyMap(),
    val javaMethods: Map<String, Methods> = emptyMap(),
    val new: Boolean = true
)

@Serializable
data class BuildSummary(
    val buildVersion: String = "",
    val addedDate: Long = 0,
    val totalMethods: Int = 0,
    val newMethods: Int = 0,
    val modifiedMethods: Int = 0,
    val unaffectedMethods: Int = 0,
    val deletedMethods: Int = 0
)

@Serializable
data class BuildSummaryWebSocket(
    val buildVersion: String,
    val alias: String,
    val addedDate: Long,
    val totalMethods: Int,
    val newMethods: Int,
    val modifiedMethods: Int,
    val unaffectedMethods: Int,
    val deletedMethods: Int
)

fun BuildSummary.toWebSocketSummary(buildAlias: String) = BuildSummaryWebSocket(
    buildVersion = this.buildVersion,
    alias = buildAlias,
    addedDate = this.addedDate,
    totalMethods = this.totalMethods,
    newMethods = this.newMethods,
    modifiedMethods = this.modifiedMethods,
    unaffectedMethods = this.unaffectedMethods,
    deletedMethods = this.deletedMethods
)

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