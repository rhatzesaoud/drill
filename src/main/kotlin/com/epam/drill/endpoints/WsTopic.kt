package com.epam.drill.endpoints

import com.epam.drill.common.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import org.kodein.di.*
import org.kodein.di.generic.*
import java.util.concurrent.*
import kotlin.collections.set
import kotlin.reflect.*
import kotlin.reflect.full.*

class WsTopic(override val kodein: Kodein) : KodeinAware {
    private val app: Application by instance()
    val pathToCallBackMapping: MutableMap<String, Pair<KClass<*>, CallbackWrapper<Any, Any>>> = ConcurrentHashMap()
    private val p = "\\{(.*)}".toRegex()

    suspend operator fun invoke(block: suspend WsTopic.() -> Unit) {
        block(this)
    }

    fun resolve(destination: String): Any {
        if (pathToCallBackMapping.isEmpty()) return ""
        val urlTokens = destination.split("/")

        val filter = pathToCallBackMapping.filter { it.key.count { c -> c == '/' } + 1 == urlTokens.size }.filter {
            var matche = true
            it.key.split("/").forEachIndexed { x, y ->
                if (y == urlTokens[x] || y.startsWith("{")) {
                } else {
                    matche = false
                }
            }
            matche
        }
        val next = filter.iterator().next()

        val parameters = next.run {
            val mutableMapOf = mutableMapOf<String, String>()
            key.split("/").forEachIndexed { x, y ->
                if (y == urlTokens[x]) {
                } else if (p.matches(y)) {
                    mutableMapOf[p.find(y)!!.groupValues[1]] = urlTokens[x]
                }
            }
            val map = mutableMapOf.map { Pair(it.key, listOf(it.value)) }
            parametersOf(* map.toTypedArray())
        }
        val param = app.feature(Locations).resolve<Any>(next.value.first, parameters)

        val result = next.value.second.resolve(param)
        return result ?: ""
    }
}

inline fun <reified R : Any> WsTopic.topic(noinline block: (R) -> Any?) {
    val findAnnotation = R::class.findAnnotation<Location>()
    val path = findAnnotation?.path!!
    @Suppress("UNCHECKED_CAST")
    pathToCallBackMapping[path] = R::class to CallbackWrapper(block) as CallbackWrapper<Any, Any>
}

class CallbackWrapper<T, R>(val block: (R) -> T?) {
    fun resolve(param: R): T? {
        return block(param)
    }
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun serialize(value: Any?): String {
    if (value == null) return ""
    if (value is String) return value
    val serializer = when (value) {
        is List<*> -> ArrayListSerializer(elementSerializer(value))
        is Set<*> -> HashSetSerializer(elementSerializer(value))
        is Map<*, *> -> HashMapSerializer(
            elementSerializer(value.keys),
            elementSerializer(value.values)
        )
        is Array<*> -> {
            @Suppress("UNCHECKED_CAST")
            (ReferenceArraySerializer(
                value::class as KClass<Any>,
                elementSerializer(value.asList()) as KSerializer<Any>
            ))
        }
        else -> value::class.serializer()
    }
    @Suppress("UNCHECKED_CAST")
    return serializer as KSerializer<Any> stringify value
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun elementSerializer(collection: Collection<*>) = (collection.firstOrNull() ?: "")::class.serializer()