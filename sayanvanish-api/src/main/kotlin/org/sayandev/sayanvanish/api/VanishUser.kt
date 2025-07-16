package org.sayandev.sayanvanish.api

import kotlinx.coroutines.Deferred
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.sayandev.sayanvanish.api.exception.UnsupportedPlatformException
import org.sayandev.sayanvanish.api.storage.PlatformTable
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

    override suspend fun save(): Deferred<Boolean> {
        serverId = Platform.get().serverId
        return VanishAPI.get().getDatabase().saveVanishUser(this)
    }

    suspend fun delete() {
        VanishAPI.get().getDatabase().removeVanishUser(uniqueId)
    }

    // TODO: implement a json type adapter for this class

    object Schema : PlatformTable("vanish_users") {
        val uniqueId = reference("unique_id", User.Schema.uniqueId).uniqueIndex()
        val isVanished = bool("is_vanished")
        val vanishLevel = integer("vanish_level")
        val currentOptions = varchar("current_options", 255)

        override val primaryKey = PrimaryKey(uniqueId)
    }

    companion object {
        // TODO: implement a generic type like User.Generic() to replace VanishUser.of() and use it for json type adapter
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