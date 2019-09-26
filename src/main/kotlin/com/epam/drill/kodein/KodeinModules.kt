package com.epam.drill.kodein

import com.epam.drill.cache.*
import com.epam.drill.cache.impl.*
import com.epam.drill.endpoints.*
import com.epam.drill.endpoints.agent.*
import com.epam.drill.endpoints.openapi.*
import com.epam.drill.endpoints.plugin.*
import com.epam.drill.endpoints.system.*
import com.epam.drill.plugin.api.end.*
import com.epam.drill.plugins.*
import com.epam.drill.service.*
import com.epam.drill.storage.*
import com.epam.drill.websockets.*
import io.ktor.application.*
import org.kodein.di.*
import org.kodein.di.generic.*

val storage: Kodein.Builder.(Application) -> Unit = { app ->
    bind<DataSourceRegistry>() with eagerSingleton { DataSourceRegistry() }
    bind<AgentStorage>() with singleton { ObservableMapStorage<String, AgentEntry, MutableSet<AgentWsSession>>() }
    bind<CacheService>() with eagerSingleton { JvmCacheService() }
    bind<AgentManager>() with eagerSingleton { AgentManager(kodein) }
    bind<MutableSet<DrillWsSession>>() with eagerSingleton { HashSet<DrillWsSession>() }
}

val wsHandler: Kodein.Builder.(Application) -> Unit = { app ->
    bind<AgentEndpoints>() with eagerSingleton { AgentEndpoints(kodein) }
    bind<Sender>() with eagerSingleton { DrillPluginWs(kodein) }
    bind<DrillServerWs>() with eagerSingleton { DrillServerWs(kodein) }
    bind<ServerWsTopics>() with eagerSingleton { ServerWsTopics(kodein) }
    bind<WsTopic>() with singleton { WsTopic() }
}

val handlers: Kodein.Builder.(Application) -> Unit = { app ->
    bind<DrillAdminEndpoints>() with eagerSingleton { DrillAdminEndpoints(kodein) }
    bind<PluginDispatcher>() with eagerSingleton { PluginDispatcher(kodein) }
    bind<InfoController>() with eagerSingleton { InfoController(kodein) }
    bind<LoginHandler>() with eagerSingleton { LoginHandler(kodein) }
    bind<AgentHandler>() with eagerSingleton { AgentHandler(kodein) }
    bind<RequestValidator>() with eagerSingleton { RequestValidator(kodein) }
}

val pluginServices: Kodein.Builder.(Application) -> Unit = { app ->
    bind<Plugins>() with singleton { Plugins() }
    bind<PluginLoaderService>() with eagerSingleton { PluginLoaderService(kodein) }
}