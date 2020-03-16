package com.epam.drill.plugin.api

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*


interface DrillPlugin<A> {
    val id: String

    val serDe: SerDe<A>

    suspend fun doAction(action: A): Any

    fun parseAction(rawAction: String) = serDe.actionSerializer.parse(rawAction)

    suspend fun doRawAction(rawAction: String) = doAction(parseAction(rawAction))

    infix fun <T> KSerializer<T>.parse(rawData: String) = serDe.parse(this, rawData)

    infix fun <T> KSerializer<T>.stringify(rawData: T) = serDe.stringify(this, rawData)
}


class SerDe<A>(
    val actionSerializer: KSerializer<A>,
    val ctx: SerialModule = EmptyModule,
    private val fmt: StringFormat = Json(context = ctx, configuration = JsonConfiguration.Stable.copy(isLenient = true))
) : StringFormat by fmt