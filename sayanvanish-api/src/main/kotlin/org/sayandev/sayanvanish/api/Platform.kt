/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.api

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.stickynote.core.configuration.Config
import java.io.File
import java.util.logging.Logger

open class Platform(
    val id: String,
    val pluginName: String,
    val logger: Logger,
    var rootDirectory: File,
    var serverId: String,
    val adapter: PlatformAdapter<out User, out VanishUser>,
    val serializers: SerializersModule = EmptySerializersModule(),
    open val featureScanPackages: Set<String> = setOf("org.sayandev.sayanvanish"),
) {

    @JvmSynthetic
    open suspend fun register() { }

    fun registerBlocking() {
        runBlocking { register() }
    }

    @JvmSynthetic
    open suspend fun unregister() {
        VanishAPI.get().getDatabase().disconnect().await()
    }

    fun unregisterBlocking() {
        runBlocking { unregister() }
    }

    companion object {
        private var currentPlatform = Platform(
            "sayanvanish",
            "SayanVanish",
            Logger.getGlobal(),
            File(".", "sayanvanish").apply {
                if (!this.exists()) {
                    this.mkdirs()
                }
            },
            "sayanvanish-virtual-server",
            APIPlatformAdapter()
        )

        @JvmStatic
        fun setPlatform(platform: Platform) {
            currentPlatform = platform
        }

        @JvmStatic
        fun register() {
            RegisteredFeatureHandler.process()
        }

        @JvmStatic
        fun setAndRegister(platform: Platform): Boolean {
            setPlatform(platform)

            Config.registerSerializersModule(platform.serializers)

            platform.registerBlocking()

            register()
            return true
        }

        @JvmStatic
        fun get() = currentPlatform
    }



}
