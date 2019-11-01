package com.epam.drill.plugin.api.message

import kotlinx.serialization.*

@Serializable
open class StatusMessage(val code: Int, val message: String)

object StatusCodes {

    const val OK = 200
    const val NOT_FOUND = 404
    const val CONFLICT = 400

}