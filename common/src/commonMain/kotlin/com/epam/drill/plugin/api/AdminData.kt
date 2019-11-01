package com.epam.drill.plugin.api

import com.epam.drill.common.*

interface AdminData {
    val buildManager: BuildManager
}

interface BuildManager {
    val buildInfos: Map<String, BuildInfo>
    val summaries: List<BuildSummary>

    operator fun get(buildVersion: String): BuildInfo?
}