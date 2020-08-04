package com.epam.drill.plugin.api

import com.epam.drill.common.*

interface AdminData {
    val buildManager: BuildManager

    val classBytes: Map<String, ByteArray>
}

interface BuildManager {
    val builds: Collection<BuildInfo>

    operator fun get(version: String): BuildInfo?
}
