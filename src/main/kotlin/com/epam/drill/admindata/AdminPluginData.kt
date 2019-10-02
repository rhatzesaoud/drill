package com.epam.drill.admindata

import com.epam.drill.common.*
import com.epam.drill.plugin.api.*
import kotlinx.atomicfu.*
import mu.*
import java.io.*
import java.util.*
import java.util.concurrent.*

const val DEV_PROPERTIES = "src/main/resources/dev.application.properties"
const val PROD_PROPERTIES = "src/main/resources/prod.application.properties"

private val logger = KotlinLogging.logger {}

typealias AdminDataVault = ConcurrentHashMap<String, AdminPluginData>

class AdminPluginData(val agentId: String, private val devMode: Boolean) : AdminData {

    private var _packagesPrefixes = atomic(PackagesPrefixes(readPackages()))

    var packagesPrefixes: PackagesPrefixes
        get() = _packagesPrefixes.value
        set(value) {
            _packagesPrefixes.value = value
        }

    override var buildManager = AgentBuildManager(agentId)

    private fun readPackages(): List<String> = Properties().run {
        val propertiesFileName = if (devMode) DEV_PROPERTIES else PROD_PROPERTIES
        getPackagesProperty(propertiesFileName)
    }

    private fun Properties.getPackagesProperty(fileName: String): List<String> = try {
        load(FileInputStream(fileName))
        getProperty("prefixes").split(",")
    } catch (ioe: IOException) {
        logger.error("Could not open properties file; packages prefixes are empty")
        emptyList()
    } catch (ise: IllegalStateException) {
        logger.error("Could not read 'prefixes' property; packages prefixes are empty")
        emptyList()
    }
}