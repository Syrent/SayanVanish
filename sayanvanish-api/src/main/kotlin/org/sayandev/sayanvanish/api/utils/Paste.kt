package org.sayandev.sayanvanish.api.utils

import org.sayandev.stickynote.lib.gson.JsonParser
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

    fun post(): CompletableFuture<String> {
        val future = CompletableFuture<String>()

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

                val key = JsonParser.parseString(response.toString()).asJsonObject.get("key").asString

                future.complete(key)
                return future
            }
        } else {
            throw IOException("Failed to upload content, HTTP response code: $responseCode")
        }
    }

    companion object {
        private const val BASE_URL = "https://api.pastes.dev"
        private const val POST_URL = "$BASE_URL/post"
        private const val USER_AGENT: String = "Mozilla/5.0"

        const val PASTE_URL = "https://paste.sayandev.org"
    }

}