/*
 * This Metrics class was auto-generated and can be copied into your project if you are
 * not using a build tool like Gradle or Maven for dependency management.
 *
 * IMPORTANT: You are not allowed to modify this class, except changing the package.
 *
 * Disallowed modifications include but are not limited to:
 *  - Remove the option for users to opt-out
 *  - Change the frequency for data submission
 *  - Obfuscate the code (every obfuscator should allow you to make an exception for specific files)
 *  - Reformat the code (if you use a linter, add an exception)
 *
 * Violations will result in a ban of your plugin and account from bStats.
 */
package org.sayandev.sayanvanish.bukkit.api

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.*
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.logging.Level
import java.util.stream.Collectors
import java.util.zip.GZIPOutputStream
import javax.net.ssl.HttpsURLConnection

class Metrics(private val plugin: Plugin, serviceId: Int) {
    private val metricsBase: MetricsBase

    /**
     * Creates a new Metrics instance.
     *
     * @param plugin Your plugin instance.
     * @param serviceId The id of the service. It can be found at [What is my plugin id?](https://bstats.org/what-is-my-plugin-id)
     */
    init {
        // Get the config file
        val bStatsFolder = File(plugin.dataFolder.parentFile, "bStats")
        val configFile = File(bStatsFolder, "config.yml")
        val config = YamlConfiguration.loadConfiguration(configFile)
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true)
            config.addDefault("serverUuid", UUID.randomUUID().toString())
            config.addDefault("logFailedRequests", false)
            config.addDefault("logSentData", false)
            config.addDefault("logResponseStatusText", false)
            // Inform the server owners about bStats
            config
                .options()
                .header(
                    ("""
                    bStats (https://bStats.org) collects some basic information for plugin authors, like how
                    many people use their plugin and their total player count. It's recommended to keep bStats
                    enabled, but if you're not comfortable with this, you can turn this setting off. There is no
                    performance penalty associated with having metrics enabled, and data sent to bStats is fully
                    anonymous.
                    """.trimIndent())
                )
                .copyDefaults(true)
            try {
                config.save(configFile)
            } catch (ignored: IOException) {
            }
        }
        // Load the data
        val enabled = config.getBoolean("enabled", true)
        val serverUUID = config.getString("serverUuid")
        val logErrors = config.getBoolean("logFailedRequests", false)
        val logSentData = config.getBoolean("logSentData", false)
        val logResponseStatusText = config.getBoolean("logResponseStatusText", false)
        var isFolia = false
        try {
            isFolia = Class.forName("io.papermc.paper.threadedregions.RegionizedServer") != null
        } catch (e: Exception) {
        }
        metricsBase =
            MetricsBase(
                "bukkit",
                serverUUID,
                serviceId,
                enabled,
                { builder: JsonObjectBuilder -> this.appendPlatformData(builder) },
                { builder: JsonObjectBuilder -> this.appendServiceData(builder) },
                if (isFolia)
                    null
                else
                    Consumer { submitDataTask: Runnable? ->
                        Bukkit.getScheduler().runTask(
                            plugin, submitDataTask!!
                        )
                    },
                { plugin.isEnabled },
                { message: String?, error: Throwable? -> plugin.logger.log(Level.WARNING, message, error) },
                { message: String? -> plugin.logger.log(Level.INFO, message) },
                logErrors,
                logSentData,
                logResponseStatusText,
                false
            )
    }

    /** Shuts down the underlying scheduler service.  */
    fun shutdown() {
        metricsBase.shutdown()
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    fun addCustomChart(chart: CustomChart) {
        metricsBase.addCustomChart(chart)
    }

    private fun appendPlatformData(builder: JsonObjectBuilder) {
        builder.appendField("playerAmount", playerAmount)
        builder.appendField("onlineMode", if (Bukkit.getOnlineMode()) 1 else 0)
        builder.appendField("bukkitVersion", Bukkit.getVersion())
        builder.appendField("bukkitName", Bukkit.getName())
        builder.appendField("javaVersion", System.getProperty("java.version"))
        builder.appendField("osName", System.getProperty("os.name"))
        builder.appendField("osArch", System.getProperty("os.arch"))
        builder.appendField("osVersion", System.getProperty("os.version"))
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors())
    }

    private fun appendServiceData(builder: JsonObjectBuilder) {
        builder.appendField("pluginVersion", plugin.description.version)
    }

    private val playerAmount: Int
        get() {
            try {
                // Around MC 1.8 the return type was changed from an array to a collection,
                // This fixes java.lang.NoSuchMethodError:
                // org.bukkit.Bukkit.getOnlinePlayers()Ljava/util/Collection;
                val onlinePlayersMethod =
                    Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers")
                return if (onlinePlayersMethod.returnType == MutableCollection::class.java)
                    (onlinePlayersMethod.invoke(Bukkit.getServer()) as Collection<*>).size
                else
                    (onlinePlayersMethod.invoke(Bukkit.getServer()) as Array<Player?>).size
            } catch (e: Exception) {
                // Just use the new method if the reflection failed
                return Bukkit.getOnlinePlayers().size
            }
        }

    class MetricsBase(
        platform: String,
        serverUuid: String?,
        serviceId: Int,
        enabled: Boolean,
        appendPlatformDataConsumer: Consumer<JsonObjectBuilder>,
        appendServiceDataConsumer: Consumer<JsonObjectBuilder>,
        submitTaskConsumer: Consumer<Runnable>?,
        checkServiceEnabledSupplier: Supplier<Boolean>,
        errorLogger: BiConsumer<String?, Throwable?>,
        infoLogger: Consumer<String?>,
        logErrors: Boolean,
        logSentData: Boolean,
        logResponseStatusText: Boolean,
        skipRelocateCheck: Boolean
    ) {
        private val scheduler: ScheduledExecutorService

        private val platform: String

        private val serverUuid: String

        private val serviceId: Int

        private val appendPlatformDataConsumer: Consumer<JsonObjectBuilder>

        private val appendServiceDataConsumer: Consumer<JsonObjectBuilder>

        private val submitTaskConsumer: Consumer<Runnable>?

        private val checkServiceEnabledSupplier: Supplier<Boolean>

        private val errorLogger: BiConsumer<String?, Throwable?>

        private val infoLogger: Consumer<String?>

        private val logErrors: Boolean

        private val logSentData: Boolean

        private val logResponseStatusText: Boolean

        private val customCharts: MutableSet<CustomChart> = HashSet()

        private val enabled: Boolean

        /**
         * Creates a new MetricsBase class instance.
         *
         * @param platform The platform of the service.
         * @param serviceId The id of the service.
         * @param serverUuid The server uuid.
         * @param enabled Whether or not data sending is enabled.
         * @param appendPlatformDataConsumer A consumer that receives a `JsonObjectBuilder` and
         * appends all platform-specific data.
         * @param appendServiceDataConsumer A consumer that receives a `JsonObjectBuilder` and
         * appends all service-specific data.
         * @param submitTaskConsumer A consumer that takes a runnable with the submit task. This can be
         * used to delegate the data collection to a another thread to prevent errors caused by
         * concurrency. Can be `null`.
         * @param checkServiceEnabledSupplier A supplier to check if the service is still enabled.
         * @param errorLogger A consumer that accepts log message and an error.
         * @param infoLogger A consumer that accepts info log messages.
         * @param logErrors Whether or not errors should be logged.
         * @param logSentData Whether or not the sent data should be logged.
         * @param logResponseStatusText Whether or not the response status text should be logged.
         * @param skipRelocateCheck Whether or not the relocate check should be skipped.
         */
        init {
            val scheduler =
                ScheduledThreadPoolExecutor(
                    1
                ) { task: Runnable? ->
                    val thread = Thread(task, "bStats-Metrics")
                    thread.isDaemon = true
                    thread
                }
            // We want delayed tasks (non-periodic) that will execute in the future to be
            // cancelled when the scheduler is shutdown.
            // Otherwise, we risk preventing the server from shutting down even when
            // MetricsBase#shutdown() is called
            scheduler.executeExistingDelayedTasksAfterShutdownPolicy = false
            this.scheduler = scheduler
            this.platform = platform
            this.serverUuid = serverUuid!!
            this.serviceId = serviceId
            this.enabled = enabled
            this.appendPlatformDataConsumer = appendPlatformDataConsumer
            this.appendServiceDataConsumer = appendServiceDataConsumer
            this.submitTaskConsumer = submitTaskConsumer
            this.checkServiceEnabledSupplier = checkServiceEnabledSupplier
            this.errorLogger = errorLogger
            this.infoLogger = infoLogger
            this.logErrors = logErrors
            this.logSentData = logSentData
            this.logResponseStatusText = logResponseStatusText
            if (!skipRelocateCheck) {
                checkRelocation()
            }
            if (enabled) {
                // WARNING: Removing the option to opt-out will get your plugin banned from
                // bStats
                startSubmitting()
            }
        }

        fun addCustomChart(chart: CustomChart) {
            customCharts.add(chart)
        }

        fun shutdown() {
            scheduler.shutdown()
        }

        private fun startSubmitting() {
            val submitTask =
                Runnable {
                    if (!enabled || !checkServiceEnabledSupplier.get()) {
                        // Submitting data or service is disabled
                        scheduler.shutdown()
                        return@Runnable
                    }
                    if (submitTaskConsumer != null) {
                        submitTaskConsumer.accept(Runnable { this.submitData() })
                    } else {
                        this.submitData()
                    }
                }
            // Many servers tend to restart at a fixed time at xx:00 which causes an uneven
            // distribution of requests on the
            // bStats backend. To circumvent this problem, we introduce some randomness into
            // the initial and second delay.
            // WARNING: You must not modify and part of this Metrics class, including the
            // submit delay or frequency!
            // WARNING: Modifying this code will get your plugin banned on bStats. Just
            // don't do it!
            val initialDelay = (1000 * 60 * (3 + Math.random() * 3)).toLong()
            val secondDelay = (1000 * 60 * (Math.random() * 30)).toLong()
            scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS)
            scheduler.scheduleAtFixedRate(
                submitTask, initialDelay + secondDelay, (1000 * 60 * 30).toLong(), TimeUnit.MILLISECONDS
            )
        }

        private fun submitData() {
            val baseJsonBuilder = JsonObjectBuilder()
            appendPlatformDataConsumer.accept(baseJsonBuilder)
            val serviceJsonBuilder = JsonObjectBuilder()
            appendServiceDataConsumer.accept(serviceJsonBuilder)
            val chartData =
                customCharts.stream()
                    .map<JsonObjectBuilder.JsonObject?> { customChart: CustomChart ->
                        customChart.getRequestJsonObject(
                            errorLogger,
                            logErrors
                        )
                    }
                    .filter { obj: JsonObjectBuilder.JsonObject? -> Objects.nonNull(obj) }
                    .toList()
                    .toTypedArray()
            serviceJsonBuilder.appendField("id", serviceId)
            serviceJsonBuilder.appendField("customCharts", chartData)
            baseJsonBuilder.appendField("service", serviceJsonBuilder.build())
            baseJsonBuilder.appendField("serverUUID", serverUuid)
            baseJsonBuilder.appendField("metricsVersion", METRICS_VERSION)
            val data = baseJsonBuilder.build()
            scheduler.execute {
                try {
                    // Send the data
                    sendData(data)
                } catch (e: Exception) {
                    // Something went wrong! :(
                    if (logErrors) {
                        errorLogger.accept("Could not submit bStats metrics data", e)
                    }
                }
            }
        }

        @Throws(Exception::class)
        private fun sendData(data: JsonObjectBuilder.JsonObject) {
            if (logSentData) {
                infoLogger.accept("Sent bStats metrics data: $data")
            }
            val url = String.format(REPORT_URL, platform)
            val connection = URL(url).openConnection() as HttpsURLConnection
            // Compress the data to save bandwidth
            val compressedData = compress(data.toString())
            connection.requestMethod = "POST"
            connection.addRequestProperty("Accept", "application/json")
            connection.addRequestProperty("Connection", "close")
            connection.addRequestProperty("Content-Encoding", "gzip")
            connection.addRequestProperty("Content-Length", compressedData!!.size.toString())
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "Metrics-Service/1")
            connection.doOutput = true
            DataOutputStream(connection.outputStream).use { outputStream ->
                outputStream.write(compressedData)
            }
            val builder = StringBuilder()
            BufferedReader(InputStreamReader(connection.inputStream)).use { bufferedReader ->
                var line: String?
                while ((bufferedReader.readLine().also { line = it }) != null) {
                    builder.append(line)
                }
            }
            if (logResponseStatusText) {
                infoLogger.accept("Sent data to bStats and received response: $builder")
            }
        }

        /** Checks that the class was properly relocated.  */
        private fun checkRelocation() {
            // You can use the property to disable the check in your test environment
            if (System.getProperty("bstats.relocatecheck") == null
                || System.getProperty("bstats.relocatecheck") != "false"
            ) {
                // Maven's Relocate is clever and changes strings, too. So we have to use this
                // little "trick" ... :D
                val defaultPackage = String(
                    byteArrayOf(
                        'o'.code.toByte(),
                        'r'.code.toByte(),
                        'g'.code.toByte(),
                        '.'.code.toByte(),
                        'b'.code.toByte(),
                        's'.code.toByte(),
                        't'.code.toByte(),
                        'a'.code.toByte(),
                        't'.code.toByte(),
                        's'.code.toByte()
                    )
                )
                val examplePackage = String(
                    byteArrayOf(
                        'y'.code.toByte(),
                        'o'.code.toByte(),
                        'u'.code.toByte(),
                        'r'.code.toByte(),
                        '.'.code.toByte(),
                        'p'.code.toByte(),
                        'a'.code.toByte(),
                        'c'.code.toByte(),
                        'k'.code.toByte(),
                        'a'.code.toByte(),
                        'g'.code.toByte(),
                        'e'.code.toByte()
                    )
                )
                // We want to make sure no one just copy & pastes the example and uses the wrong
                // package names
                check(
                    !(MetricsBase::class.java.getPackage().name.startsWith(defaultPackage)
                            || MetricsBase::class.java.getPackage().name.startsWith(examplePackage))
                ) { "bStats Metrics class has not been relocated correctly!" }
            }
        }

        companion object {
            /** The version of the Metrics class.  */
            const val METRICS_VERSION: String = "3.1.0"

            private const val REPORT_URL = "https://bStats.org/api/v2/data/%s"

            /**
             * Gzips the given string.
             *
             * @param str The string to gzip.
             * @return The gzipped string.
             */
            @Throws(IOException::class)
            private fun compress(str: String?): ByteArray? {
                if (str == null) {
                    return null
                }
                val outputStream = ByteArrayOutputStream()
                GZIPOutputStream(outputStream).use { gzip ->
                    gzip.write(str.toByteArray(StandardCharsets.UTF_8))
                }
                return outputStream.toByteArray()
            }
        }
    }

    class AdvancedBarChart
    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String, private val callable: Callable<Map<String, IntArray>>) :
        CustomChart(chartId) {
        @get:Throws(Exception::class)
        override val chartData: JsonObjectBuilder.JsonObject?
            get() {
                val valuesBuilder =
                    JsonObjectBuilder()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                var allSkipped = true
                for ((key, value) in map) {
                    if (value.size == 0) {
                        // Skip this invalid
                        continue
                    }
                    allSkipped = false
                    valuesBuilder.appendField(key, value)
                }
                if (allSkipped) {
                    // Null = skip the chart
                    return null
                }
                return JsonObjectBuilder()
                    .appendField("values", valuesBuilder.build()).build()
            }
    }

    class SimplePie
    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String, private val callable: Callable<String>) : CustomChart(chartId) {
        @get:Throws(Exception::class)
        override val chartData: JsonObjectBuilder.JsonObject?
            get() {
                val value = callable.call()
                if (value == null || value.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                return JsonObjectBuilder().appendField("value", value)
                    .build()
            }
    }

    class DrilldownPie
    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String, private val callable: Callable<Map<String, Map<String, Int>>>) :
        CustomChart(chartId) {
        @get:Throws(Exception::class)
        override val chartData: JsonObjectBuilder.JsonObject?
            get() {
                val valuesBuilder =
                    JsonObjectBuilder()
                val map =
                    callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                var reallyAllSkipped = true
                for ((key) in map) {
                    val valueBuilder =
                        JsonObjectBuilder()
                    var allSkipped = true
                    for ((key1, value) in map[key]!!) {
                        valueBuilder.appendField(key1, value)
                        allSkipped = false
                    }
                    if (!allSkipped) {
                        reallyAllSkipped = false
                        valuesBuilder.appendField(key, valueBuilder.build())
                    }
                }
                if (reallyAllSkipped) {
                    // Null = skip the chart
                    return null
                }
                return JsonObjectBuilder()
                    .appendField("values", valuesBuilder.build()).build()
            }
    }

    class SingleLineChart
    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String, private val callable: Callable<Int>) : CustomChart(chartId) {
        @get:Throws(Exception::class)
        override val chartData: JsonObjectBuilder.JsonObject?
            get() {
                val value = callable.call()
                if (value == 0) {
                    // Null = skip the chart
                    return null
                }
                return JsonObjectBuilder().appendField("value", value)
                    .build()
            }
    }

    class MultiLineChart
    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String, private val callable: Callable<Map<String, Int>>) :
        CustomChart(chartId) {
        @get:Throws(Exception::class)
        override val chartData: JsonObjectBuilder.JsonObject?
            get() {
                val valuesBuilder =
                    JsonObjectBuilder()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                var allSkipped = true
                for ((key, value) in map) {
                    if (value == 0) {
                        // Skip this invalid
                        continue
                    }
                    allSkipped = false
                    valuesBuilder.appendField(key, value)
                }
                if (allSkipped) {
                    // Null = skip the chart
                    return null
                }
                return JsonObjectBuilder()
                    .appendField("values", valuesBuilder.build()).build()
            }
    }

    class AdvancedPie
    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String, private val callable: Callable<Map<String, Int>>) :
        CustomChart(chartId) {
        @get:Throws(Exception::class)
        override val chartData: JsonObjectBuilder.JsonObject?
            get() {
                val valuesBuilder =
                    JsonObjectBuilder()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                var allSkipped = true
                for ((key, value) in map) {
                    if (value == 0) {
                        // Skip this invalid
                        continue
                    }
                    allSkipped = false
                    valuesBuilder.appendField(key, value)
                }
                if (allSkipped) {
                    // Null = skip the chart
                    return null
                }
                return JsonObjectBuilder()
                    .appendField("values", valuesBuilder.build()).build()
            }
    }

    abstract class CustomChart protected constructor(chartId: String) {
        private val chartId: String

        init {
            requireNotNull(chartId) { "chartId must not be null" }
            this.chartId = chartId
        }

        fun getRequestJsonObject(
            errorLogger: BiConsumer<String?, Throwable?>, logErrors: Boolean
        ): JsonObjectBuilder.JsonObject? {
            val builder = JsonObjectBuilder()
            builder.appendField("chartId", chartId)
            try {
                val data = chartData
                    ?: // If the data is null we don't send the chart.
                    return null
                builder.appendField("data", data)
            } catch (t: Throwable) {
                if (logErrors) {
                    errorLogger.accept("Failed to get data for custom chart with id $chartId", t)
                }
                return null
            }
            return builder.build()
        }

        @get:Throws(Exception::class)
        protected abstract val chartData: JsonObjectBuilder.JsonObject?
    }

    class SimpleBarChart
    /**
     * Class constructor.
     *
     * @param chartId The id of the chart.
     * @param callable The callable which is used to request the chart data.
     */(chartId: String, private val callable: Callable<Map<String, Int>>) :
        CustomChart(chartId) {
        @get:Throws(Exception::class)
        override val chartData: JsonObjectBuilder.JsonObject?
            get() {
                val valuesBuilder =
                    JsonObjectBuilder()
                val map = callable.call()
                if (map == null || map.isEmpty()) {
                    // Null = skip the chart
                    return null
                }
                for ((key, value) in map) {
                    valuesBuilder.appendField(key, intArrayOf(value))
                }
                return JsonObjectBuilder()
                    .appendField("values", valuesBuilder.build()).build()
            }
    }

    /**
     * An extremely simple JSON builder.
     *
     *
     * While this class is neither feature-rich nor the most performant one, it's sufficient enough
     * for its use-case.
     */
    class JsonObjectBuilder {
        private var builder: StringBuilder? = StringBuilder()

        private var hasAtLeastOneField = false

        init {
            builder!!.append("{")
        }

        /**
         * Appends a null field to the JSON.
         *
         * @param key The key of the field.
         * @return A reference to this object.
         */
        fun appendNull(key: String): JsonObjectBuilder {
            appendFieldUnescaped(key, "null")
            return this
        }

        /**
         * Appends a string field to the JSON.
         *
         * @param key The key of the field.
         * @param value The value of the field.
         * @return A reference to this object.
         */
        fun appendField(key: String, value: String): JsonObjectBuilder {
            requireNotNull(value) { "JSON value must not be null" }
            appendFieldUnescaped(key, "\"" + escape(value) + "\"")
            return this
        }

        /**
         * Appends an integer field to the JSON.
         *
         * @param key The key of the field.
         * @param value The value of the field.
         * @return A reference to this object.
         */
        fun appendField(key: String, value: Int): JsonObjectBuilder {
            appendFieldUnescaped(key, value.toString())
            return this
        }

        /**
         * Appends an object to the JSON.
         *
         * @param key The key of the field.
         * @param object The object.
         * @return A reference to this object.
         */
        fun appendField(key: String, `object`: JsonObject): JsonObjectBuilder {
            requireNotNull(`object`) { "JSON object must not be null" }
            appendFieldUnescaped(key, `object`.toString())
            return this
        }

        /**
         * Appends a string array to the JSON.
         *
         * @param key The key of the field.
         * @param values The string array.
         * @return A reference to this object.
         */
        fun appendField(key: String, values: Array<String>): JsonObjectBuilder {
            requireNotNull(values) { "JSON values must not be null" }
            val escapedValues =
                Arrays.stream(values)
                    .map { value: String -> "\"" + escape(value) + "\"" }
                    .collect(Collectors.joining(","))
            appendFieldUnescaped(key, "[$escapedValues]")
            return this
        }

        /**
         * Appends an integer array to the JSON.
         *
         * @param key The key of the field.
         * @param values The integer array.
         * @return A reference to this object.
         */
        fun appendField(key: String, values: IntArray): JsonObjectBuilder {
            requireNotNull(values) { "JSON values must not be null" }
            val escapedValues =
                Arrays.stream(values).mapToObj { i: Int -> java.lang.String.valueOf(i) }
                    .collect(Collectors.joining(","))
            appendFieldUnescaped(key, "[$escapedValues]")
            return this
        }

        /**
         * Appends an object array to the JSON.
         *
         * @param key The key of the field.
         * @param values The integer array.
         * @return A reference to this object.
         */
        fun appendField(key: String, values: Array<JsonObject>): JsonObjectBuilder {
            requireNotNull(values) { "JSON values must not be null" }
            val escapedValues =
                Arrays.stream(values).map { obj: JsonObject -> obj.toString() }.collect(Collectors.joining(","))
            appendFieldUnescaped(key, "[$escapedValues]")
            return this
        }

        /**
         * Appends a field to the object.
         *
         * @param key The key of the field.
         * @param escapedValue The escaped value of the field.
         */
        private fun appendFieldUnescaped(key: String, escapedValue: String) {
            checkNotNull(builder) { "JSON has already been built" }
            requireNotNull(key) { "JSON key must not be null" }
            if (hasAtLeastOneField) {
                builder!!.append(",")
            }
            builder!!.append("\"").append(escape(key)).append("\":").append(escapedValue)
            hasAtLeastOneField = true
        }

        /**
         * Builds the JSON string and invalidates this builder.
         *
         * @return The built JSON string.
         */
        fun build(): JsonObject {
            checkNotNull(builder) { "JSON has already been built" }
            val `object` = JsonObject(
                builder!!.append("}").toString()
            )
            builder = null
            return `object`
        }

        /**
         * A super simple representation of a JSON object.
         *
         *
         * This class only exists to make methods of the [JsonObjectBuilder] type-safe and not
         * allow a raw string inputs for methods like [JsonObjectBuilder.appendField].
         */
        class JsonObject(private val value: String) {
            override fun toString(): String {
                return value
            }
        }

        companion object {
            /**
             * Escapes the given string like stated in https://www.ietf.org/rfc/rfc4627.txt.
             *
             *
             * This method escapes only the necessary characters '"', '\'. and '\u0000' - '\u001F'.
             * Compact escapes are not used (e.g., '\n' is escaped as "\u000a" and not as "\n").
             *
             * @param value The value to escape.
             * @return The escaped value.
             */
            private fun escape(value: String): String {
                val builder = StringBuilder()
                for (i in 0..<value.length) {
                    val c = value[i]
                    if (c == '"') {
                        builder.append("\\\"")
                    } else if (c == '\\') {
                        builder.append("\\\\")
                    } else if (c <= '\u000F') {
                        builder.append("\\u000").append(Integer.toHexString(c.code))
                    } else if (c <= '\u001F') {
                        builder.append("\\u00").append(Integer.toHexString(c.code))
                    } else {
                        builder.append(c)
                    }
                }
                return builder.toString()
            }
        }
    }
}