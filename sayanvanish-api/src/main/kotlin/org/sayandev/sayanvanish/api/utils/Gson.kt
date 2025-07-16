package org.sayandev.sayanvanish.api.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Gson {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    @JvmStatic
    fun get(): Gson {
        return gson
    }
}