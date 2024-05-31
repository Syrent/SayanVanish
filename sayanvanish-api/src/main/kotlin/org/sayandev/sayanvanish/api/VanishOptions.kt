package org.sayandev.sayanvanish.api

import org.sayandev.stickynote.core.utils.Gson
import org.sayandev.stickynote.lib.gson.JsonObject
import org.sayandev.stickynote.lib.gson.JsonParser

data class VanishOptions(
    var sendMessage: Boolean = true,
    var notifyOthers: Boolean = true,
) {

    class Builder {
        private var sendMessage = true
        private var notifyOthers = true

        fun sendMessage(sendMessage: Boolean): Builder {
            this.sendMessage = sendMessage
            return this
        }

        fun notifyOthers(notifyOthers: Boolean): Builder {
            this.notifyOthers = notifyOthers
            return this
        }

        fun build(): VanishOptions {
            return VanishOptions(sendMessage, notifyOthers)
        }
    }

    fun toJson(): String {
        val json = JsonObject()
        json.addProperty("send-message", sendMessage)
        json.addProperty("notify-others", notifyOthers)
        return Gson.gson.toJson(json)
    }

    companion object {
        @JvmStatic
        fun fromJson(serialized: String): VanishOptions {
            val json = JsonParser.parseString(serialized).asJsonObject
            return VanishOptions(
                json.get("send-message").asBoolean,
                json.get("notify-others").asBoolean
            )
        }

        @JvmStatic
        fun defaultOptions(): VanishOptions {
            return VanishOptions()
        }
    }
}