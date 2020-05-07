package com.epam.drill.plugin.api.processing

import com.epam.drill.session.*

object DrillContext : IDrillContex {
    override operator fun invoke(): String? = threadStorage.get()?.drillSessionId?.ifEmpty { null }
    override operator fun get(key: String): String? = threadStorage.get()?.headers?.get(key.toLowerCase())
}

interface IDrillContex {
    operator fun invoke(): String?
    operator fun get(key: String): String?
}
