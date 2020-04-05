package com.epam.drill.session

import com.epam.drill.plugin.DrillRequest


lateinit var threadStorage: InheritableThreadLocal<DrillRequest>
