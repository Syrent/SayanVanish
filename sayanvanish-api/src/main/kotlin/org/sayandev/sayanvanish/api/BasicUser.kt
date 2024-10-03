package org.sayandev.sayanvanish.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.sayandev.sayanvanish.api.exception.UnsupportedPlatformException
import java.util.*

interface BasicUser {

    val uniqueId: UUID
    var username: String
    var serverId: String

    fun hasPermission(permission: String): Boolean {
        throw UnsupportedPlatformException("hasPermission")
    }

    fun hasPermission(permission: Permission): Boolean {
        return hasPermission(permission.permission())
    }

    fun save() {
        SayanVanishAPI.getInstance().database.addBasicUser(this)
    }

    fun toJson(): String {
        val json = JsonObject()
        json.addProperty("unique-id", uniqueId.toString())
        json.addProperty("username", username)
        json.addProperty("server-id", serverId)
        return Gson().toJson(json)
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
                override var serverId: String = serverId ?: Platform.get().serverId
            }
        }
    }

}