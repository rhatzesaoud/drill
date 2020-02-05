package com.epam.drill.logger

import io.ktor.util.date.*
import mu.*

class NativeLogger(val name: String) : KLogger {
    override fun trace(msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL trace] ${msg()}")
    }

    override fun trace(t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL trace] ${msg()}")
    }

    override fun trace(marker: Marker?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL trace] ${msg()}")
    }

    override fun trace(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL trace] ${msg()}")
    }

    override fun debug(msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL debug] ${msg()}")
    }

    override fun debug(t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL debug] ${msg()}")
    }

    override fun debug(marker: Marker?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL debug] ${msg()}")
    }

    override fun debug(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL debug] ${msg()}")
    }

    override fun info(msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL info] ${msg()}")
    }

    override fun info(t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL info] ${msg()}")
    }

    override fun info(marker: Marker?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL info] ${msg()}")
    }

    override fun info(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL info] ${msg()}")
    }

    override fun warn(msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL warn] ${msg()}")
    }

    override fun warn(t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL warn] ${msg()}")
    }

    override fun warn(marker: Marker?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL warn] ${msg()}")
    }

    override fun warn(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL warn] ${msg()}")
    }

    override fun error(msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL error] ${msg()}")
    }

    override fun error(t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL error] ${msg()}")
    }

    override fun error(marker: Marker?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL error] ${msg()}")
    }

    override fun error(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        println("${GMTDate().toLogDate()} [$name][DRILL error] ${msg()}")
    }

    override fun entry(vararg argArray: Any?) {
        throw NotImplementedError("An operation is not implemented")
    }

    override fun exit() {
        throw NotImplementedError("An operation is not implemented")
    }

    override fun <T> exit(result: T): T {
        throw NotImplementedError("An operation is not implemented")
    }

    override fun <T : Throwable> throwing(throwable: T): T {
        throw NotImplementedError("An operation is not implemented")
    }

    override fun <T : Throwable> catching(throwable: T) {
        throw NotImplementedError("An operation is not implemented")
    }

}

fun GMTDate.toLogDate(): String =
    "${year.padZero(4)}-${month.ordinal.padZero(2)}-${dayOfMonth.padZero(2)} ${hours.padZero(2)}:${minutes.padZero(2)}:${seconds.padZero(
        2
    )} GTM"


private fun Int.padZero(length: Int): String = toString().padStart(length, '0')

