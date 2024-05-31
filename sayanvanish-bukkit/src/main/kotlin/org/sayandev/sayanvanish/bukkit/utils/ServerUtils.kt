package org.sayandev.sayanvanish.bukkit.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.bukkit.plugin.Plugin
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.server
import oshi.SystemInfo
import java.time.Instant


object ServerUtils {

    val gson = GsonBuilder().setPrettyPrinting().create()

    fun getServerData(additionalData: Map<String, String> = emptyMap()): String {
        val jsonObject = JsonObject()
        jsonObject.add("paste-info", JsonObject().apply {
            this.addProperty("instant", Instant.now().toString())
        })

        jsonObject.add("machine", JsonObject().apply {
            this.addProperty("operating-system", System.getProperty("os.name"))
            this.addProperty("processor", SystemInfo().hardware.processor.processorIdentifier.name)
            this.addProperty("available-processors", Runtime.getRuntime().availableProcessors())
            this.addProperty("free-memory", Runtime.getRuntime().freeMemory() / 1024)
            val maxMemory = Runtime.getRuntime().maxMemory()
            this.addProperty("max-memory", if (maxMemory == Long.MAX_VALUE) -1 else (maxMemory / 1024))
            this.addProperty("system-memory", SystemInfo().hardware.memory.total / 1024)
        })

        jsonObject.add("server", JsonObject().apply {
            this.addProperty("name", server.name)
            this.addProperty("version", server.version)
            this.addProperty("bukkit-version", server.bukkitVersion)
            this.addProperty("motd", server.motd)
            this.addProperty("online-mode", server.onlineMode)
            this.addProperty("port", server.port)
            this.addProperty("players", onlinePlayers.joinToString(", ") { it.name })
            this.addProperty("operators", server.operators.filter { it.player != null }.joinToString(", ") { it.name ?: it.uniqueId.toString() })
            this.addProperty("plugins", server.pluginManager.plugins.joinToString(", ") { it.name })
        })

        jsonObject.add("plugin", serializePlugin(plugin))

        jsonObject.add("plugins", JsonArray().apply {
            server.pluginManager.plugins.filter { it != plugin }.forEach { serverPlugin ->
                this.add(serializePlugin(serverPlugin))
            }
        })

        jsonObject.add("additional-data", JsonObject().apply {
            additionalData.map { this.addProperty(it.key, it.value) }
        })

        return gson.toJson(jsonObject)
    }

    fun serializePlugin(plugin: Plugin): JsonObject {
        val description = plugin.description
        return JsonObject().apply {
            this.add(description.name, JsonObject().apply {
                this.addProperty("version", description.version)
                this.addProperty("api-version", description.apiVersion)
                this.addProperty("libraries", description.libraries.joinToString(", "))
                this.addProperty("depend", description.depend.joinToString(", "))
                this.addProperty("soft-depend", description.softDepend.joinToString(", "))
            })
        }
    }
}