package com.epam.drill.plugin.api.end

suspend fun AdminPluginPart<*>.sendToAgent(
    buildVersion: String = agentInfo.buildVersion,
    destination: Any,
    message: Any
) = sender.send(
    context = AgentSendContext(id, buildVersion),
    destination = destination,
    message = message
)

suspend fun AdminPluginPart<*>.sendToGroup(destination: Any, message: Any) = sender.send(
    context = GroupSendContext(agentInfo.serviceGroup),
    destination = destination,
    message = message
)

data class AgentSendContext(
    val agentId: String,
    val buildVersion: String
) : SendContext

data class GroupSendContext(
    val groupId: String
) : SendContext

@Deprecated(
    message = "This method is no longer acceptable",
    replaceWith = ReplaceWith("AdminPluginPart<*>.sendToAgent(...)")
)
suspend fun Sender.send(
    agentId: String,
    buildVersion: String,
    destination: Any,
    message: Any
) = send(AgentSendContext(agentId, buildVersion), destination, message)
