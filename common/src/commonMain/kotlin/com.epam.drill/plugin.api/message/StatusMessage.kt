package com.epam.drill.plugin.api.message

import kotlinx.serialization.*

@Deprecated("Messages will be defined in plugins.")
@Serializable
open class StatusMessage(val code: Int, val message: String)

@Deprecated("Codes will be defined in plugins.")
object StatusCodes {

    const val OK = 200
    const val NOT_FOUND = 404
    const val CONFLICT = 400

}
