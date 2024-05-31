package org.sayandev.sayanvanish.api

import org.sayandev.stickynote.core.utils.Gson
import org.sayandev.stickynote.lib.gson.JsonObject
import org.sayandev.stickynote.lib.gson.JsonParser
import java.util.*

interface BasicUser {

    val uniqueId: UUID
    var username: String
    var serverId: String

    fun save() {
        SayanVanishAPI.getInstance().addBasicUser(this)
    }

    fun toJson(): String {
        val json = JsonObject()
        json.addProperty("unique-id", uniqueId.toString())
        json.addProperty("username", username)
        json.addProperty("server-id", serverId)
        return Gson.gson.toJson(json)
    }

    companion object {
        @JvmStatic
        fun fromJson(serialized: String): BasicUser {
            val json = JsonParser.parseString(serialized).asJsonObject
            val uniqueId = json.get("unique-id").asString
            val username = json.get("username").asString
            val serverId = json.get("server-id").asString
            return create(UUID.fromString(uniqueId), username, serverId)
        }

        @JvmStatic
        fun create(uniqueId: UUID, username: String, serverId: String?): BasicUser {
            return object : BasicUser {
                override val uniqueId: UUID = uniqueId
                override var username: String = username
                override var serverId: String = serverId ?: Platform.get().id
            }
        }
    }

}