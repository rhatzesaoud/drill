package com.epam.drill.agent

import com.epam.drill.*
import com.epam.drill.cache.*
import com.epam.drill.cache.impl.*
import com.epam.drill.endpoints.*
import com.epam.drill.endpoints.agent.*
import com.epam.drill.jwt.config.*
import com.epam.drill.kodein.*
import com.epam.drill.service.*
import com.epam.drill.storage.*
import com.epam.drill.websockets.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.config.*
import io.ktor.locations.*
import io.ktor.websocket.*
import org.kodein.di.generic.*

val testApp: Application.(String) -> Unit = { sslPort ->
    (environment.config as MapApplicationConfig).apply {
        put("ktor.deployment.sslPort", sslPort)
    }
    install(Locations)
    install(WebSockets)
    install(Authentication) {
        jwt {
            realm = "Drill4J app"
            verifier(JwtConfig.verifier)
            validate {
                it.payload.getClaim("id").asInt()?.let(userSource::findUserById)
            }
        }
    }
    kodeinApplication(AppBuilder {
        withKModule { kodeinModule("wsHandler", wsHandler) }
        withKModule {
            kodeinModule("test") {
                bind<AgentStorage>() with eagerSingleton { AgentStorage() }
                bind<CacheService>() with eagerSingleton { JvmCacheService() }
                bind<SessionStorage>() with eagerSingleton { mutableSetOf<DrillWsSession>() }
                bind<LoginHandler>() with eagerSingleton { LoginHandler(kodein) }
                bind<AgentHandler>() with eagerSingleton { AgentHandler(kodein) }
                bind<RequestValidator>() with eagerSingleton { RequestValidator(kodein) }

            }

        }
    })
}