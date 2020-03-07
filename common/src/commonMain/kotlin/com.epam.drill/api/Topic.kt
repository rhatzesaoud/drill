package com.epam.drill.api

import kotlinx.serialization.*
import kotlin.reflect.*

@SerialInfo
annotation class Topic(val url: String)


@ImplicitReflectionSerializer
inline fun <reified T : Any> KClass<T>.topicUrl() = (this
    .serializer())
    .descriptor
    .annotations
    .filterIsInstance<Topic>()
    .first()
    .url