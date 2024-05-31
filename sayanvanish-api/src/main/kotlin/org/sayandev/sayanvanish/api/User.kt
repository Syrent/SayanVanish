package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.exception.UnsupportedPlatformException
import org.sayandev.stickynote.core.utils.Gson
import org.sayandev.stickynote.lib.gson.JsonObject
import org.sayandev.stickynote.lib.gson.JsonParser
import java.util.*
import kotlin.reflect.KClass

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

    override fun save() {
        SayanVanishAPI.getInstance().addUser(this)
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

        fun User.cast(to: KClass<out User>): Any {
            return to.java.getDeclaredMethod("fromUser", User::class.java).invoke(null, this)
        }
    }

}