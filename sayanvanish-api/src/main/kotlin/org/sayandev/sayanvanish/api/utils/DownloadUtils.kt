package org.sayandev.sayanvanish.api.utils

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

object DownloadUtils {

    fun download(url: String, destination: File): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        val url = URL(url)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = BufferedInputStream(connection.inputStream)
            val outputStream = FileOutputStream(destination)

            val buffer = ByteArray(1024)
            var bytesRead = inputStream.read(buffer)

            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer)
            }

            outputStream.close()
            inputStream.close()

            future.complete(true)
        } else {
            future.complete(false)
        }
        connection.disconnect()

        return future
    }
}