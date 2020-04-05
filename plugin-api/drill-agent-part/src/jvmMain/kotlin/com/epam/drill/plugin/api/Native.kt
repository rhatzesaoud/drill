package com.epam.drill.plugin.api

object Native {
    fun RetransformClasses(classes: Array<Class<*>>) {
        RetransformClasses(classes.size, classes)
    }

    external fun RetransformClasses(count: Int, classes: Array<Class<*>>)
    external fun RetransformClassesByPackagePrefixes(prefixes: Array<String>)
    external fun GetAllLoadedClasses(): Array<Class<*>>
}