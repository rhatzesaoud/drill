package com.epam.drill.plugins.coverage

import kotlinx.serialization.*

@Polymorphic
@Serializable
abstract class Action

@SerialName("START")
@Serializable
data class StartNewSession(val payload: StartPayload) : Action()

@SerialName("START_AGENT_SESSION")
@Serializable
data class StartSession(val payload: StartSessionPayload) : Action()

@SerialName("STOP")
@Serializable
data class StopSession(val payload: SessionPayload) : Action()

@SerialName("CANCEL")
@Serializable
data class CancelSession(val payload: SessionPayload) : Action()
@Serializable
data class StartPayload(val testType: String = "MANUAL", val sessionId: String = "")

@Serializable
data class StartSessionPayload(val sessionId: String, val startPayload: StartPayload)

@Serializable
data class SessionPayload(val sessionId: String)