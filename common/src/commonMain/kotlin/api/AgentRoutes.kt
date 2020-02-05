package com.epam.drill.api

import kotlinx.serialization.*

sealed class Communication {
    sealed class Agent {
        @Serializable
        @Topic("/agent/load")
        class PluginLoadEvent

        @Serializable
        @Topic("/agent/unload")
        class PluginUnloadEvent

        @Serializable
        @Topic("/agent/load-classes-data")
        class LoadClassesDataEvent

        @Serializable
        @Topic("/agent/set-packages-prefixes")
        class SetPackagePrefixesEvent

        @Serializable
        @Topic("/agent/update-config")
        class UpdateConfigEvent

        @Serializable
        @Topic("/agent/change-header-name")
        class ChangeHeaderNameEvent

        @Serializable
        @Topic("/agent/toggle")
        class ToggleEvent
    }

    sealed class Plugin {

        @Serializable
        @Topic("/plugin/updatePluginConfig")
        class UpdateConfigEvent

        @Serializable
        @Topic("/plugin/action")
        class DispatchEvent

        @Serializable
        @Topic("/plugin/togglePlugin")
        class ToggleEvent

        @Serializable
        @Topic("/plugin/unload")
        class UnloadEvent

        @Serializable
        @Topic("/plugin/resetPlugin")
        class ResetEvent
    }
}

const val AGENT_ATTACH_URL = "/agent/attach"