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

import com.google.gson.JsonParser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture


class Paste(
    val language: String,
    val content: List<String>,
) {

    suspend fun post(): Deferred<String> {
        val deferred = CompletableDeferred<String>()

        val url = URL(POST_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestMethod("POST")
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.setRequestProperty("Content-Type", "text/$language")
        connection.setDoOutput(true)
        connection.connectTimeout = 10000

        connection.outputStream.use { os ->
            val input: ByteArray = content.joinToString("\n").byteInputStream(StandardCharsets.UTF_8).readAllBytes()
            os.write(input, 0, input.size)
        }

        val responseCode: Int = connection.getResponseCode()
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { br ->
                var inputLine: String?
                val response = StringBuilder()

                while ((br.readLine().also { inputLine = it }) != null) {
                    response.append(inputLine)
                }

                val body = response.toString()
                val key =
                    runCatching {
                        JsonParser.parseString(body).asJsonObject.get("key").asString
                    }.getOrNull()
                        ?: connection.getHeaderField("Location")
                        ?: throw IOException("Paste API did not return a key.")

                deferred.complete(key)
                return deferred
            }
        } else {
            throw IOException("Failed to upload content, HTTP response code: $responseCode")
        }
    }

    companion object {
        private const val BASE_URL = "https://api.pastes.dev"
        private const val POST_URL = "$BASE_URL/post"
        private const val USER_AGENT: String = "Mozilla/5.0"

        const val PASTE_URL = BASE_URL

        fun url(key: String): String {
            return "$PASTE_URL/$key"
        }
    }

}
