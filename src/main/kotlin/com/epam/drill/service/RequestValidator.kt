package com.epam.drill.service

import com.epam.drill.common.*
import com.epam.drill.endpoints.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.*
import org.kodein.di.generic.*

const val agentIsBusyMessage =
    "Sorry, this agent is busy at the moment. Please try again later"

class RequestValidator(override val kodein: Kodein) : KodeinAware {
    val app: Application by instance()
    val am: AgentManager by instance()

    init {
        app.routing {
            intercept(ApplicationCallPipeline.Call) {
                if (context is RoutingApplicationCall) {
                    val agentId = context.parameters["agentId"]
                    if (agentId != null) {
                        val agentInfo = am.getOrNull(agentId)
                        if (agentInfo?.status == null) {
                            call.respond(HttpStatusCode.BadRequest, "Agent '$agentId' not found")
                            return@intercept finish()
                        } else if (agentInfo.status == AgentStatus.BUSY) {
                            call.respond(HttpStatusCode.BadRequest, agentIsBusyMessage)
                            return@intercept finish()
                        }

                    }
                }
            }
        }
    }

}