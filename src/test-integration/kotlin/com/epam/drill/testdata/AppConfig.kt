package com.epam.drill.testdata

import com.epam.drill.endpoints.WsTopic
import com.epam.drill.jwt.config.JwtConfig
import com.epam.drill.kodein.*
import com.epam.drill.userSource
import com.epam.kodux.StoreManger
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.config.MapApplicationConfig
import io.ktor.locations.Locations
import io.ktor.websocket.WebSockets
import org.junit.rules.TemporaryFolder
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.singleton

class AppConfig(var projectDir: TemporaryFolder) {
    lateinit var wsTopic: WsTopic

    val testApp: Application.(String) -> Unit = { sslPort ->
        (environment.config as MapApplicationConfig).apply {
            put("ktor.deployment.sslPort", sslPort)
            put("ktor.dev", "true")
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
            withKModule { kodeinModule("storage", storage) }
            withKModule { kodeinModule("wsHandler", wsHandler) }
            withKModule { kodeinModule("handlers", handlers) }
            withKModule { kodeinModule("pluginServices", pluginServices) }
            val baseLocation = projectDir.newFolder("xs").resolve("agent")
            withKModule {
                kodeinModule("addition") {
                    bind<StoreManger>() with eagerSingleton {
                        StoreManger(baseLocation)
                    }
                    bind<WsTopic>() with singleton {
                        wsTopic = WsTopic(kodein)
                        wsTopic
                    }
                }
            }
        })
    }
}