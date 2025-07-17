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