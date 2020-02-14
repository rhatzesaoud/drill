package com.epam.drill.plugin.api.processing

import kotlinx.cinterop.*
import kotlin.native.concurrent.*

class NativePluginApi(
    val pluginId: String,
    val jvmti: CPointer<*>?,
    val jvm: CPointer<*>?,
    val clb: CPointer<*>?,
    val sender: CPointer<CFunction<(pluginId: CPointer<ByteVar>, message: CPointer<ByteVar>) -> Unit>>
)

@SharedImmutable
val natContex = Worker.start(true)

@ThreadLocal
var api: NativePluginApi? = null

@ThreadLocal
var plugin: NativePart<*>? = null


inline fun <reified T> pluginApi(noinline what: NativePluginApi.() -> T) =
    natContex.execute(TransferMode.UNSAFE, { what }) {
        it(api!!)
    }.result
