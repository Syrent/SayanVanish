package org.sayandev.sayanvanish.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.jetbrains.exposed.v1.core.Table
import org.sayandev.sayanvanish.api.exception.UnsupportedPlatformException
import java.util.*

interface VanishUser : User {
    var currentOptions: VanishOptions
    var isVanished: Boolean
    var vanishLevel: Int

    suspend fun disappear(options: VanishOptions) {
        isVanished = true
        save()
    }

    suspend fun disappear() {
        disappear(VanishOptions.defaultOptions())
    }

    suspend fun appear(options: VanishOptions) {
        isVanished = false
        save()
    }

    suspend fun appear() {
        appear(VanishOptions.defaultOptions())
    }

    suspend fun toggleVanish(options: VanishOptions) {
        if (isVanished) appear(options) else disappear(options)
    }

    suspend fun toggleVanish() {
        toggleVanish(VanishOptions.defaultOptions())
    }

    fun sendComponent(content: String, vararg placeholder: TagResolver) {
        Platform.get().adapter.adapt(this).sendComponent(content, *placeholder)
    }

    fun sendActionbar(content: String, vararg placeholder: TagResolver) {
        Platform.get().adapter.adapt(this).sendActionbar(content, *placeholder)
    }

    /**
    * @param otherVanishUser The user to check if this user can see
    * */
    fun canSee(otherVanishUser: VanishUser): Boolean {
        if (!otherVanishUser.isVanished) return true
        if (this.uniqueId == otherVanishUser.uniqueId) return true
        val canSee = vanishLevel >= otherVanishUser.vanishLevel
        return canSee
    }

    override suspend fun save() {
        serverId = Platform.get().serverId
        SayanVanishAPI.getDatabase().addVanishUser(this)
    }

    suspend fun delete() {
        SayanVanishAPI.getDatabase().removeVanishUser(uniqueId)
    }

    override fun toJson(): String {
        val json = JsonObject()
        json.addProperty("unique-id", uniqueId.toString())
        json.addProperty("username", username)
        json.addProperty("is-online", isOnline)
        json.addProperty("server-id", serverId)
        json.addProperty("is-vanished", isVanished)
        json.addProperty("vanish-level", vanishLevel)
        json.addProperty("current-options", currentOptions.toJson())
        return Gson().toJson(json)
    }

    object Schema : Table("${Platform.get().pluginName.lowercase()}_vanish_users") {
        val uniqueId = reference("unique_id", User.Schema.uniqueId).uniqueIndex()
        val isVanished = bool("is_vanished")
        val vanishLevel = integer("vanish_level")
        val currentOptions = varchar("current_options", 255)

        override val primaryKey = PrimaryKey(uniqueId)
    }

    companion object {
        @JvmStatic
        fun fromJson(serialized: String): VanishUser {
            val json = JsonParser.parseString(serialized).asJsonObject

            val uniqueId = json.get("unique-id").asString
            val username = json.get("username").asString
            val isOnline = json.get("is-online").asBoolean
            val serverId = json.get("server-id").asString
            val isVanished = json.get("is-vanished").asBoolean
            val vanishLevel = json.get("vanish-level").asInt
            val currentOptions = json.get("current-options").asString

            return of(
                UUID.fromString(uniqueId),
                username,
                serverId,
                isVanished,
                isOnline,
                vanishLevel,
                VanishOptions.fromJson(currentOptions)
            )
        }

        fun of(
            uniqueId: UUID,
            username: String,
            serverId: String = Platform.get().serverId,
            isVanished: Boolean = false,
            isOnline: Boolean = false,
            vanishLevel: Int = 1,
            currentOptions: VanishOptions = VanishOptions.defaultOptions()
        ): VanishUser {
            return object : VanishUser {
                override val uniqueId: UUID = uniqueId
                override var username: String = username
                override var serverId: String = serverId
                override var currentOptions: VanishOptions = currentOptions
                override var isVanished: Boolean = isVanished
                override var isOnline: Boolean = isOnline
                override var vanishLevel: Int = vanishLevel
                override fun sendComponent(
                    content: String,
                    vararg placeholder: TagResolver
                ) {
                    throw UnsupportedPlatformException("sendComponent")
                }

                override fun sendActionbar(
                    content: String,
                    vararg placeholder: TagResolver
                ) {
                    throw UnsupportedPlatformException("sendActionbar")
                }
            }
        }
    }

}