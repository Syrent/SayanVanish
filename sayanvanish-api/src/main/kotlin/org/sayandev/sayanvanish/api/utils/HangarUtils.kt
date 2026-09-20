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
import com.google.gson.annotations.SerializedName
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

object HangarUtils {
    private val gson = Gson()

    fun getLatestRelease(): CompletableFuture<VersionInfo> {
        return get("https://hangar.papermc.io/api/v1/projects/sayanvanish/latestrelease")
    }

    fun getLatestSnapshot(): CompletableFuture<VersionInfo> {
        return get("https://hangar.papermc.io/api/v1/projects/sayanvanish/latest?channel=Snapshot")
    }

    private fun get(urlString: String): CompletableFuture<VersionInfo> {
        val future = CompletableFuture<VersionInfo>()

        CompletableFuture.runAsync {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "text/plain")

            val version = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readLine() }

            val versionUrl = URL("$HANGAR_API_URL/$version")
            val versionConnection = versionUrl.openConnection() as HttpURLConnection
            versionConnection.requestMethod = "GET"
            versionConnection.setRequestProperty("User-Agent", USER_AGENT)
            versionConnection.setRequestProperty("Accept", "application/json")

            val versionInfo = BufferedReader(InputStreamReader(versionConnection.inputStream)).use { reader ->
                gson.fromJson(reader, VersionInfo::class.java)!!
            }


            future.complete(versionInfo)
        }

        return future
    }

    private const val HANGAR_API_URL = "https://hangar.papermc.io/api/v1/projects/sayanvanish/versions"
    private const val USER_AGENT: String = "Mozilla/5.0"
}

data class VersionInfo(
    val name: String,
    val description: String,
    val downloads: Downloads
)

data class Downloads(
    @SerializedName("PAPER") val PAPER: PlatformInfo?,
    @SerializedName("WATERFALL") val WATERFALL: PlatformInfo?,
    @SerializedName("VELOCITY") val VELOCITY: PlatformInfo?
)

data class PlatformInfo(
    val externalUrl: String?,
    @SerializedName("downloadUrl") val downloadUrl: String?
) {
    fun downloadUrl(): String {
        return externalUrl ?: downloadUrl ?: ""
    }
}