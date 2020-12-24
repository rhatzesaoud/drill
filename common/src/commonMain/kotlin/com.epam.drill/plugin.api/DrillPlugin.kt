package com.epam.drill.plugin.api


interface DrillPlugin<A> {
    val id: String

    suspend fun doAction(action: A): Any

    fun parseAction(rawAction: String): A

    suspend fun doRawAction(rawAction: String) = doAction(parseAction(rawAction))

}
