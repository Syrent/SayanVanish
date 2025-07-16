package org.sayandev.sayanvanish.api.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject

object Gson {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    @JvmStatic
    fun get(): Gson {
        return gson
    }

    fun Any.jsonObject(): JsonObject {
        if (this::class.isData) {
            return com.google.gson.Gson().toJsonTree(this).asJsonObject
        } else {
            return JsonObject().apply {
                this.javaClass.declaredFields.map { field ->
                    field.isAccessible = true
                    val name = field.name.replaceFirstChar { char -> char.lowercase() }
                    when (field.type) {
                        String::class.java -> addProperty(name, field.get(this) as String)
                        Number::class.java -> addProperty(name, field.get(this) as Number)
                        Boolean::class.java -> addProperty(name, field.get(this) as Boolean)
                        Char::class.java -> addProperty(name, field.get(this) as Char)
                        else -> addProperty(name, field.get(this).toString())
                    }
                }
            }
        }
    }

    inline fun <reified T> JsonObject.fromJson(): T {
        if (T::class.isData) {
            return com.google.gson.Gson().fromJson(this, T::class.java)
        } else if (!T::class.java.isInterface) {
            val clazz = T::class.java
            val constructor = clazz.constructors.firstOrNull { constructor ->
                constructor.parameters.all { parameter -> this.get(parameter.name) != null }
            } ?: throw IllegalArgumentException("Tried to generate instance of type ${T::class.java.simpleName} but no suitable constructor was found for provided JsonObject.")
            val args = constructor.parameters.map { parameter ->
                val jsonElement: JsonElement? = this.get(parameter.name)
                when (parameter.type) {
                    String::class.java -> jsonElement?.asString ?: ""
                    Number::class.java -> jsonElement?.asNumber ?: 0
                    Boolean::class.java -> jsonElement?.asBoolean ?: false
                    Char::class.java -> jsonElement?.asString?.firstOrNull() ?: ' '
                    else -> throw IllegalArgumentException("Unsupported type ${parameter.type} for parameter ${parameter.name}")
                }
            }.toTypedArray()
            return constructor.newInstance(*args) as T
        }
    }
}