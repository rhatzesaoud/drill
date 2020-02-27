package com.epam.drill.plugin.api

import com.epam.drill.common.*

interface AdminData {
    val buildManager: BuildManager
}

interface BuildManager {
    @Deprecated(message = "Exposure of inner state", replaceWith = ReplaceWith("builds"))
    val buildInfos: Map<String, BuildInfo>

    val builds: Collection<BuildInfo>

    operator fun get(version: String): BuildInfo?
}
