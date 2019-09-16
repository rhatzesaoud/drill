package com.epam.drill.core.ws

import com.epam.drill.common.*
import com.epam.drill.common.ws.*
import com.epam.drill.core.*
import com.epam.drill.logger.*
import com.epam.drill.plugin.*
import com.epam.drill.plugin.api.processing.*
import kotlinx.serialization.*
import kotlin.collections.set
import kotlin.native.concurrent.*

@SharedImmutable
val topicLogger = DLogger("topicLogger")

fun topicRegister() =
    WsRouter {

        topic(DrillEvent.SYNC_STARTED.name).rawMessage { topicLogger.info { "Agent synchronization is started" } }
        topic(DrillEvent.SYNC_FINISHED.name).rawMessage {
            exec { agentConfig.needSync = false }
            topicLogger.info { "Agent synchronization is finished" }
        }

        topic("/agent/config").withGenericTopic(ServiceConfig.serializer()) { sc ->
            topicLogger.info { "Agent got a system config: $sc" }
            exec { secureAdminAddress = adminAddress.copy(scheme = "https", defaultPort = sc.sslPort.toInt()) }
        }

        topic("/plugins/unload").rawMessage { pluginId ->
            topicLogger.warn { "Unload event. Plugin id is $pluginId" }
            PluginManager[pluginId]?.unload(UnloadReason.ACTION_FROM_ADMIN)
            println(
                """
                |________________________________________________________
                |Physical Deletion is not implemented yet.
                |We should unload all resource e.g. classes, jars, .so/.dll
                |Try to create custom classLoader. After this full GC.
                |________________________________________________________
            """.trimMargin()
            )
        }

        topic("/plugins/agent-attached").rawMessage {
            topicLogger.warn { "Agent is attached" }
        }

        topic("/plugins/updatePluginConfig").withGenericTopic(PluginConfig.serializer()) { config ->
            topicLogger.warn { "UpdatePluginConfig event: message is $config " }
            val agentPluginPart = PluginManager[config.id]
            if (agentPluginPart != null) {
                agentPluginPart.setEnabled(false)
                agentPluginPart.off()
                agentPluginPart.updateRawConfig(config)
                agentPluginPart.np?.updateRawConfig(config)
                agentPluginPart.setEnabled(true)
                agentPluginPart.on()
                topicLogger.warn { "New settings for ${config.id} saved" }
            } else
                topicLogger.warn { "Plugin ${config.id} not loaded to agent" }

        }

        topic("/plugins/action").withGenericTopic(PluginAction.serializer()) { m ->
            topicLogger.warn { "actionPluign event: message is ${m.message} " }
            val agentPluginPart = PluginManager[m.id]
            agentPluginPart?.doRawAction(m.message)

        }

        topic("/plugins/togglePlugin").rawMessage { pluginId ->
            topicLogger.warn { "togglePlugin event: PluginId is $pluginId" }
            val agentPluginPart = PluginManager[pluginId]
            if (agentPluginPart != null)
                agentPluginPart.setEnabled(!agentPluginPart.isEnabled())
            else
                topicLogger.warn { "Plugin $pluginId not loaded to agent" }

        }

        topic("agent/toggleStandBy").rawMessage {
            topicLogger.warn { "toggleStandBy event" }
        }
    }


@ThreadLocal
object WsRouter {

    val mapper = mutableMapOf<String, Topic>()
    operator fun invoke(alotoftopics: WsRouter.() -> Unit) {
        alotoftopics(this)
    }


    @Suppress("ClassName")
    open class inners(open val destination: String) {
        fun <T> withGenericTopic(des: KSerializer<T>, block: suspend (T) -> Unit): GenericTopic<T> {
            val genericTopic = GenericTopic(destination, des, block)
            mapper[destination] = genericTopic
            return genericTopic
        }


        @Suppress("unused")
        fun withFileTopic(block: suspend (message: String, file: ByteArray) -> Unit): FileTopic {
            val fileTopic = FileTopic(destination, block)
            mapper[destination] = fileTopic
            return fileTopic
        }


        fun rawMessage(block: suspend (String) -> Unit): InfoTopic {
            val infoTopic = InfoTopic(destination, block)
            mapper[destination] = infoTopic
            return infoTopic
        }

    }

    operator fun get(topic: String): Topic? {
        return mapper[topic]
    }

}

@Suppress("unused")
fun WsRouter.topic(url: String): WsRouter.inners {
    return WsRouter.inners(url)
}

open class Topic(open val destination: String)

class GenericTopic<T>(
    override val destination: String,
    private val deserializer: KSerializer<T>,
    val block: suspend (T) -> Unit
) : Topic(destination) {
    suspend fun deserializeAndRun(message: String) {
        block(deserializer parse message)
    }
}

class InfoTopic(
    override val destination: String,
    val block: suspend (String) -> Unit
) : Topic(destination)


open class FileTopic(
    override val destination: String,
    @Suppress("unused") open val block: suspend (message: String, file: ByteArray) -> Unit
) : Topic(destination)

@Suppress("unused")
class PluginTopic(
    override val destination: String,
    newBlock: suspend (message: String, plugin: ByteArray) -> Unit
) : FileTopic(destination, newBlock)
