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
package org.sayandev.sayanvanish.api.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.api.storage.serializers.gson.UUIDJsonAdapter
import java.util.UUID

object Gson {
    private val gson = GsonBuilder()
        .registerTypeAdapter(UUID::class.java, UUIDJsonAdapter())
        .registerTypeAdapter(VanishUser::class.java, VanishUser.JsonAdapter())
        .registerTypeAdapter(VanishUser::class.java, VanishUser.JsonAdapter())
        .setPrettyPrinting()
        .create()

    @JvmStatic
    fun get(): Gson {
        return gson
    }
}