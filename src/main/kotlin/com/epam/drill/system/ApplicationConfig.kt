package com.epam.drill.system

import io.ktor.application.*

fun Application.securePort(): String {
    val sslPort = environment.config
        .config("ktor")
        .config("deployment")
        .property("sslPort")
        .getString()
    return sslPort
}