package com.epam.drill.session

import com.epam.drill.plugin.DrillRequest

object DrillRequest {
    lateinit var threadStorage: InheritableThreadLocal<DrillRequest>

    fun RetransformClasses(classes: Array<Class<*>>) {
        RetransformClasses(classes.size, classes)

    }

    external fun RetransformClasses(count: Int, classes: Array<Class<*>>)
    external fun GetAllLoadedClasses(): Array<Class<*>>

}