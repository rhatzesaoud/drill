package com.epam.drill.kodein

import com.epam.drill.cache.CacheService
import com.epam.drill.cache.impl.HazelcastCacheService
import com.epam.drill.endpoints.*
import com.epam.drill.endpoints.agent.AgentEndpoints
import com.epam.drill.endpoints.agent.AgentHandler
import com.epam.drill.endpoints.agent.AgentWsSession
import com.epam.drill.endpoints.agent.DrillServerWs
import com.epam.drill.endpoints.openapi.DrillAdminEndpoints
import com.epam.drill.endpoints.plugin.DrillPluginWs
import com.epam.drill.endpoints.plugin.PluginDispatcher
import com.epam.drill.endpoints.system.InfoController
import com.epam.drill.plugin.api.end.Sender
import com.epam.drill.plugins.PluginLoaderService
import com.epam.drill.plugins.Plugins
import com.epam.drill.service.DataSourceRegistry
import com.epam.drill.storage.AgentStorage
import com.epam.drill.storage.ObservableMapStorage
import com.epam.drill.websockets.LoginHandler
import io.ktor.application.Application
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.singleton

val storage: Kodein.Builder.(Application) -> Unit = { app ->
    bind<DataSourceRegistry>() with eagerSingleton { DataSourceRegistry() }
    bind<AgentStorage>() with singleton { ObservableMapStorage<String, AgentEntry, MutableSet<AgentWsSession>>() }
    bind<CacheService>() with eagerSingleton { HazelcastCacheService() }
    bind<AgentManager>() with eagerSingleton { AgentManager(kodein) }
    bind<MutableSet<DrillWsSession>>() with eagerSingleton { HashSet<DrillWsSession>() }
}

val wsHandler: Kodein.Builder.(Application) -> Unit = { app ->
    bind<AgentEndpoints>() with eagerSingleton { AgentEndpoints(kodein) }
    bind<Sender>() with eagerSingleton { DrillPluginWs(kodein) }
    bind<DrillServerWs>() with eagerSingleton {DrillServerWs(kodein) }
    bind<ServerWsTopics>() with eagerSingleton { ServerWsTopics(kodein) }
    bind<WsTopic>() with singleton { WsTopic() }
}

val handlers: Kodein.Builder.(Application) -> Unit = { app ->
    bind<DrillAdminEndpoints>() with eagerSingleton { DrillAdminEndpoints(kodein) }
    bind<PluginDispatcher>() with eagerSingleton { PluginDispatcher(kodein) }
    bind<InfoController>() with eagerSingleton { InfoController(kodein) }
    bind<LoginHandler>() with eagerSingleton { LoginHandler(kodein) }
    bind<AgentHandler>() with eagerSingleton {AgentHandler(kodein)}
}

val pluginServices: Kodein.Builder.(Application) -> Unit = { app ->
    bind<Plugins>() with singleton { Plugins() }
    bind<PluginLoaderService>() with eagerSingleton { PluginLoaderService(kodein) }
}