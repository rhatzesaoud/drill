@file:Suppress("SpellCheckingInspection")

package com.epam.drill.core.methodbind

import com.epam.drill.core.*
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*
import kotlinx.serialization.*
import kotlin.math.*

const val SocketDispatcher = "Lsun/nio/ch/SocketDispatcher;"
const val FileDispatcherImpl = "Lsun/nio/ch/FileDispatcherImpl;"
const val Netty = "Lio/netty/channel/unix/FileDescriptor;"

fun readAddress(
    env: CPointer<JNIEnvVar>,
    clazz: jclass,
    fd: Int,
    address: DirectBufferAddress,
    pos: jint,
    limit: jint
): Int {
    initRuntimeIfNeeded()
    val retVal = exec { originalMethod[::readAddress] }(env, clazz, fd, address, pos, limit)
    read(retVal, address)
    return retVal
}

fun read0(env: CPointer<JNIEnvVar>, obj: jobject, fd: jobject, address: DirectBufferAddress, len: jint): Int {
    initRuntimeIfNeeded()
    val retVal = exec { originalMethod[::read0] }(env, obj, fd, address, len)
    read(retVal, address)
    return retVal
}

private fun read(retVal: Int, address: DirectBufferAddress) {
    if (retVal > 8) {
        val prefix = address.rawString(min(8, retVal))
        defineHttp1RequestType(prefix, address, retVal)
    }
}

private fun defineHttp1RequestType(prefix: String, address: DirectBufferAddress, retVal: Int) {
    try {
        if (prefix.startsWith("OPTIONS ") ||
            prefix.startsWith("GET ") ||
            prefix.startsWith("HEAD ") ||
            prefix.startsWith("POST ") ||
            prefix.startsWith("PUT ") ||
            prefix.startsWith("PATCH ") ||
            prefix.startsWith("DELETE ") ||
            prefix.startsWith("TRACE ") ||
            prefix.startsWith("CONNECT ")
        ) {
            fillRequestToHolder(address.rawString(retVal))
        }

    } catch (ex: Throwable) {
        println(ex.message)
    }
}

fun fillRequestToHolder(@Suppress("UNUSED_PARAMETER") request: String) {
    val requestHolderClass = FindClass("com/epam/drill/ws/RequestHolder")
    @Suppress("UNUSED_VARIABLE") val selfMethodId: jfieldID? =
        GetStaticFieldID(requestHolderClass, "INSTANCE", "Lcom/epam/drill/ws/RequestHolder;")
    val requestHolder: jobject? = GetStaticObjectField(requestHolderClass, selfMethodId)
    val retrieveClassesData: jmethodID? =
        GetMethodID(requestHolderClass, "storeRequest", "(Ljava/lang/String;)V")
    CallVoidMethod(requestHolder, retrieveClassesData, NewStringUTF(request))
}

fun readv0(env: CPointer<JNIEnvVar>, obj: jobject, fd: jobject, address: DirectBufferAddress, len: jint): Int =
    read0(env, obj, fd, address, len)


fun write0(env: CPointer<JNIEnvVar>, obj: jobject, fd: jobject, address: DirectBufferAddress, len: jint): jint {
    initRuntimeIfNeeded()
    val fakeLength: jint
    val fakeBuffer: DirectBufferAddress
    val prefix = address.rawString(min(4, len))
    if (prefix == "HTTP" || prefix == "POST" || prefix == "GET ") {
        val spyHeaders = exec {
            val adminUrl = if (::secureAdminAddress.isInitialized) {
                secureAdminAddress.toUrlString(false)
            } else adminAddress.toUrlString(false)
            "\ndrill-agent-id: ${agentConfig.id}\ndrill-admin-url: $adminUrl\nsession-id: hj"
        }
        val contentBodyBytes = address.toPointer().toKStringFromUtf8()
        return if (contentBodyBytes.contains("text/html") || contentBodyBytes.contains("application/json")) {
            val replaceFirst = contentBodyBytes.replaceFirst("\n", "$spyHeaders\n")
            val toUtf8Bytes = replaceFirst.toUtf8Bytes()
            val refTo = toUtf8Bytes.refTo(0)
            val scope = Arena()
            fakeBuffer = refTo.getPointer(scope).toLong()
            val additionalSize = spyHeaders.toUtf8Bytes().size
            fakeLength = len + additionalSize
            println("write0: " + replaceFirst.lines())
            exec { originalMethod[::write0] }(env, obj, fd, fakeBuffer, fakeLength)
            scope.clear()
            len
        } else {
            exec { originalMethod[::write0] }(env, obj, fd, address, len)
            len
        }
    } else {
        exec { originalMethod[::write0] }(env, obj, fd, address, len)
        return len
    }
}

fun writeAddress(
    env: CPointer<JNIEnvVar>,
    clazz: jclass,
    fd: jint,
    address: jlong,
    pos: jint,
    limit: jint
): jint {
    initRuntimeIfNeeded()
    val fakeLength: jint
    val fakeBuffer: DirectBufferAddress
    val prefix = address.rawString(min(4, limit))
    if (prefix == "HTTP" || prefix == "POST" || prefix == "GET ") {
        val spyHeaders = exec {
            val adminUrl = if (::secureAdminAddress.isInitialized) {
                secureAdminAddress.toUrlString(false)
            } else adminAddress.toUrlString(false)
            "\ndrill-agent-id: ${agentConfig.id}\ndrill-admin-url: $adminUrl"
        }
        val contentBodyBytes = address.toPointer().toKStringFromUtf8()
        return if (contentBodyBytes.contains("text/html") || contentBodyBytes.contains("application/json")) {
            val replaceFirst = contentBodyBytes.replaceFirst("\n", "$spyHeaders\n")
            val toUtf8Bytes = replaceFirst.toUtf8Bytes()
            val refTo = toUtf8Bytes.refTo(0)
            val scope = Arena()
            fakeBuffer = refTo.getPointer(scope).toLong()
            val additionalSize = spyHeaders.toUtf8Bytes().size
            println("writeAddress: " + replaceFirst.lines())
            fakeLength = limit + additionalSize
            exec { originalMethod[::writeAddress] }(env, clazz, fd, fakeBuffer, pos, fakeLength)
            scope.clear()
            limit
        } else {
            exec { originalMethod[::writeAddress] }(env, clazz, fd, address, pos, limit)
            limit
        }
    } else {
        exec { originalMethod[::writeAddress] }(env, clazz, fd, address, pos, limit)
        return limit
    }
}