package com.epam.drill.core.concurrency

import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.native.concurrent.*

@SharedImmutable
private val dispatcher = newSingleThreadContext("BackgroundThread coroutine")

object BackgroundThread : CoroutineScope {

    operator fun <T> invoke(block: suspend () -> T) = launch { block() }

    override val coroutineContext: CoroutineContext = dispatcher

}

@SharedImmutable
private val dispatcher2 = newSingleThreadContext("BackgroundThread coroutinew")

object BackgroundThread2 : CoroutineScope {

    operator fun <T> invoke(block: suspend () -> T) = launch { block() }

    override val coroutineContext: CoroutineContext = dispatcher2

}

@SharedImmutable
private val dispatcher3 = newSingleThreadContext("BackgroundThread coroutinew")

object BackgroundThread3 : CoroutineScope {

    operator fun <T> invoke(block: suspend () -> T) = launch { block() }

    override val coroutineContext: CoroutineContext = dispatcher3
}
