package org.sayandev.sayanvanish.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jetbrains.exposed.v1.core.Table
import org.sayandev.sayanvanish.api.exception.UnsupportedPlatformException
import java.util.*

interface User {

    val uniqueId: UUID
    var username: String
    var isOnline: Boolean
    var serverId: String

    fun hasPermission(permission: String): Boolean {
        throw UnsupportedPlatformException("hasPermission")
    }

    fun hasPermission(permission: Permission): Boolean {
        return hasPermission(permission.permission())
    }

    suspend fun save() {
        SayanVanishAPI.getDatabase().addUser(this)
    }

    fun toJson(): String {
        val json = JsonObject()
        json.addProperty("unique-id", uniqueId.toString())
        json.addProperty("username", username)
        json.addProperty("is-online", isOnline)
        json.addProperty("server-id", serverId)
        return Gson().toJson(json)
    }

    object Schema : Table("${Platform.get().pluginName.lowercase()}_users") {
        val uniqueId = uuid("unique_id").uniqueIndex()
        val username = varchar("username", 16)
        val isOnline = bool("is_online").default(false)
        val serverId = varchar("server_id", 36)

        override val primaryKey = PrimaryKey(uniqueId)
    }

    companion object {
        @JvmStatic
        fun fromJson(serialized: String): User {
            val json = JsonParser.parseString(serialized).asJsonObject
            val uniqueId = json.get("unique-id").asString
            val username = json.get("username").asString
            val isOnline = json.get("is-online").asBoolean
            val serverId = json.get("server-id").asString
            return of(UUID.fromString(uniqueId), username, isOnline, serverId)
        }

        @JvmStatic
        fun of(uniqueId: UUID, username: String, isOnline: Boolean, serverId: String?): User {
            return object : User {
                override val uniqueId: UUID = uniqueId
                override var username: String = username
                override var isOnline: Boolean = isOnline
                override var serverId: String = serverId ?: Platform.get().serverId
            }
        }
    }

}