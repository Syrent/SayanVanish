package org.sayandev.sayanvanish.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.sayandev.sayanvanish.api.exception.UnsupportedPlatformException
import org.sayandev.stickynote.core.utils.Gson
import java.util.*

interface User : BasicUser {

    var currentOptions: VanishOptions
    var isVanished: Boolean
    var isOnline: Boolean
    var vanishLevel: Int

    fun vanish(options: VanishOptions) {
        isVanished = true
        save()
    }

    fun vanish() {
        vanish(VanishOptions.defaultOptions())
    }

    fun unVanish(options: VanishOptions) {
        isVanished = false
        save()
    }

    fun unVanish() {
        unVanish(VanishOptions.defaultOptions())
    }

    fun toggleVanish(options: VanishOptions) {
        if (isVanished) unVanish(options) else vanish(options)
    }

    fun toggleVanish() {
        toggleVanish(VanishOptions.defaultOptions())
    }

    open fun sendMessage(content: String) {
        throw UnsupportedPlatformException("sendMessage")
    }

    open fun sendActionbar(content: String) {
        throw UnsupportedPlatformException("sendActionbar")
    }

    open fun hasPermission(permission: String): Boolean {
        throw UnsupportedPlatformException("hasPermission")
    }

    fun hasPermission(permission: Permission): Boolean {
        return hasPermission(permission.permission())
    }

    fun canSee(otherUser: User): Boolean {
        return vanishLevel >= otherUser.vanishLevel
    }

    override fun save() {
        serverId = Platform.get().serverId
        SayanVanishAPI.getInstance().database.addUser(this)
    }

    fun delete() {
        SayanVanishAPI.getInstance().database.removeUser(uniqueId)
    }

    override fun toJson(): String {
        val json = JsonObject()
        json.addProperty("unique-id", uniqueId.toString())
        json.addProperty("username", username)
        json.addProperty("is-vanished", isVanished)
        json.addProperty("is-online", isOnline)
        json.addProperty("vanish-level", vanishLevel)
        json.addProperty("current-options", currentOptions.toJson())
        return Gson.gson.toJson(json)
    }

    companion object {
        @JvmStatic
        fun fromJson(serialized: String): User {
            val json = JsonParser.parseString(serialized).asJsonObject
            val uniqueId = json.get("unique-id").asString
            val username = json.get("username").asString
            val isVanished = json.get("is-vanished").asBoolean
            val isOnline = json.get("is-online").asBoolean
            val vanishLevel = json.get("vanish-level").asInt
            val currentOptions = VanishOptions.fromJson(json.get("current-options").asString)
            return object : User {
                override val uniqueId = UUID.fromString(uniqueId)
                override var username = username
                override var isVanished = isVanished
                override var isOnline = isOnline
                override var vanishLevel = vanishLevel
                override var currentOptions = currentOptions
                override var serverId = Platform.get().id
            }
        }

        fun User.convert(to: Class<out User>): Any {
            val instance = to.getDeclaredMethod("fromUser", User::class.java).invoke(null, this)
            return instance
        }
    }

}