package com.epam.drill.plugin.api.end

interface Sender {
    suspend fun send(context: SendContext, destination: Any, message: Any)
}

interface SendContext
