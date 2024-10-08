package org.sayandev.sayanvanish.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class VanishOptions(
    var sendMessage: Boolean = true,
    var notifyStatusChangeToOthers: Boolean = true,
    var notifyJoinQuitVanished: Boolean = true,
    var isOnJoin: Boolean = false,
    var isOnQuit: Boolean = false,
) {

    class Builder {
        private var sendMessage = true
        private var notifyStatusChangeToOthers = true
        private var notifyJoinQuitVanished = true
        private var isOnJoin = false
        private var isOnQuit = false

        fun sendMessage(sendMessage: Boolean): Builder {
            this.sendMessage = sendMessage
            return this
        }

        fun notifyStatusChangeToOthers(notifyStatusChangeToOthers: Boolean): Builder {
            this.notifyStatusChangeToOthers = notifyStatusChangeToOthers
            return this
        }

        fun notifyJoinQuitVanished(notifyJoinQuitVanished: Boolean): Builder {
            this.notifyJoinQuitVanished = notifyJoinQuitVanished
            return this
        }

        fun isOnJoin(isOnJoin: Boolean): Builder {
            this.isOnJoin = isOnJoin
            return this
        }

        fun isOnQuit(isOnQuit: Boolean): Builder {
            this.isOnQuit = isOnQuit
            return this
        }

        fun build(): VanishOptions {
            return VanishOptions(sendMessage, notifyStatusChangeToOthers, notifyJoinQuitVanished, isOnJoin)
        }
    }

    fun toJson(): String {
        val json = JsonObject()
        json.addProperty("send-message", sendMessage)
        json.addProperty("notify-status-change-to-others", notifyStatusChangeToOthers)
        json.addProperty("notify-join-quit-vanished", notifyJoinQuitVanished)
        json.addProperty("is-on-join", isOnJoin)
        json.addProperty("is-on-quit", isOnQuit)
        return Gson().toJson(json)
    }

    companion object {
        @JvmStatic
        fun fromJson(serialized: String): VanishOptions {
            val json = JsonParser.parseString(serialized).asJsonObject
            return VanishOptions(
                json.get("send-message").asBoolean,
                json.get("notify-status-change-to-others").asBoolean,
                json.get("notify-join-quit-vanished").asBoolean,
                json.get("is-on-join").asBoolean
            )
        }

        @JvmStatic
        fun defaultOptions(): VanishOptions {
            return VanishOptions()
        }
    }
}