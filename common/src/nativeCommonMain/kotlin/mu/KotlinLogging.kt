package mu

import com.epam.drill.logger.*


object KotlinLogging {
    /**
     * This method allow defining the logger in a file in the following way:
     * ```
     * val logger = KotlinLogging.logger {}
     * ```
     */
    fun logger(func: () -> Unit): KLogger {
        val message = func::class.qualifiedName?.replace(".${func::class.simpleName}", "")
        return NativeLogger(message ?: "unknown logger")
    }

    fun logger(name: String): KLogger {
        return NativeLogger(name)
    }
}
