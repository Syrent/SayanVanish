import io.papermc.hangarpublishplugin.model.Platforms
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.9.10"
    id("maven-publish")
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("org.screamingsandals.nms-mapper") version "1.4.5"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("io.papermc.hangar-publish-plugin") version "0.1.0"
}

val slug = "velocityvanish"
group = "ir.syrent.velocityvanish"
version = "${project.version}"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.maven.apache.org/maven2/")

    // Velocity-API / PaperLib / Folia
    maven("https://repo.papermc.io/repository/maven-public/")

    // AdventureAPI/MiniMessage
    maven("https://oss.sonatype.org/content/repositories/snapshots/")

    // Spigot
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")

    // ProtocolLib
    maven("https://repo.dmulloy2.net/repository/public/")

    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    // EssentialsX
    maven("https://repo.essentialsx.net/releases/")

    // Update checker
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")

    // DiscordSRV
    maven("https://nexus.scarsz.me/content/groups/public/")

    // Mojang
    maven("https://libraries.minecraft.net/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.3")
    compileOnly("xyz.jpenilla:squaremap-api:1.1.16")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("org.spongepowered:configurate-yaml:4.2.0-yamlbranch-4")
    compileOnly("com.discordsrv:discordsrv:1.26.2")
    compileOnly("com.mojang:authlib:1.11")
    compileOnly("io.netty:netty-all:4.1.96.Final")

    // SayanChat 2.8.1
    // ProCosmetics
    // ServerListPlus Velocity (From extradeps)
    // SunLight
    // NexEngine (From extradeps)
    // CMI (From extradeps)
    compileOnly(fileTree("libs"))

    implementation("io.papermc:paperlib:1.0.8")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    // gson does not exist in legacy minecraft version
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.github.cryptomorin:XSeries:9.5.0") { isTransitive = false }

    implementation("net.kyori:adventure-api:4.14.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")

    implementation("cloud.commandframework:cloud-paper:1.9.0-SNAPSHOT")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.9.0-SNAPSHOT")

    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}



publishing {
    publications {
        register<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = rootProject.name

            artifact(tasks.shadowJar.get().archiveFile)
        }
    }

    publishing {
        repositories {
            maven {
                name = "syrent"
                url = uri("https://jitpack.io")
            }
        }
    }

    tasks.withType<PublishToMavenLocal> {
        dependsOn(tasks.shadowJar)
    }
}

val extraDependencies = mapOf(
    "CMI.jar" to "https://www.zrips.net/wp-content/uploads/2021/09/CMI9.0.0.0API.jar",
    "NexEngine.jar" to "https://github.com/nulli0n/NexEngine-spigot/releases/download/v2.2.11/NexEngine.jar",
    "SunLight.jar" to "https://www.dropbox.com/scl/fi/mmu7g32vj6oitkja65cws/SunLight-3.9.4.jar?rlkey=tqxgcy80yccxh80yzl24ubs7b&dl=1",
    "ServerListPlus.jar" to "https://ci.codemc.io/job/Minecrell/job/ServerListPlus/lastSuccessfulBuild/artifact/Velocity/build/libs/ServerListPlus-3.5.1-SNAPSHOT-Velocity.jar",
)

tasks {
    runServer {
        minecraftVersion("1.20.1")
        serverJar(file("run/paper-1.20.1-48.jar"))
    }
    runPaper {
        folia.registerTask()
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching(listOf("**plugin.yml", "**plugin.json")) {
            expand(
                "version" to project.version,
                "slug" to slug,
                "name" to rootProject.name,
                "description" to project.description
            )
        }
    }

    val extraDeps = register("downloadExtraDependencies") {
        val libsDir = File("libs")
        libsDir.mkdirs()
        val ex = Executors.newCachedThreadPool()
        for (entry in extraDependencies) {
            val file = File(libsDir, entry.key)
            if (file.exists())
                continue
            ex.submit {
                println("Downloading ${entry.key} from ${entry.value}")
                URL(entry.value).openStream().use { s -> file.outputStream().use { it.write(s.readBytes()) } }
                println("Successfully downloaded ${entry.key} to ${file.path}")
            }
        }
        ex.shutdown()
        ex.awaitTermination(10, TimeUnit.SECONDS)
    }

    shadowJar {
        dependsOn(extraDeps)
        archiveClassifier.set("")
        exclude("META-INF/**")
        from("LICENSE")
        minimize()

        relocate("io.papermc.lib", "ir.syrent.velocityvanish.dependencies.io.papermc.lib")
        relocate("io.leangen", "ir.syrent.velocityvanish.dependencies.io.leangen")
        relocate("org.bstats", "ir.syrent.velocityvanish.dependencies.org.bstats")
        relocate("com.google.code.gson", "ir.syrent.velocityvanish.dependencies.com.google.code.gson")
        relocate("com.cryptomorin", "ir.syrent.velocityvanish.dependencies.com.github.cryptomorin")
        relocate("cloud.commandframework", "ir.syrent.velocityvanish.dependencies.cloud.commandframework")
        relocate("kotlin", "ir.syrent.velocityvanish.dependencies.kotlin")
        relocate("com.jeff_media", "ir.syrent.velocityvanish.dependencies.com.jeff_media")
        relocate("org.jetbrains", "ir.syrent.velocityvanish.dependencies.org.jetbrains")
        relocate("org.intellij", "ir.syrent.velocityvanish.dependencies.org.intellij")
    }

    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

fun executeGitCommand(vararg command: String): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", *command)
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}

fun latestCommitMessage(): String {
    return executeGitCommand("log", "-1", "--pretty=%B")
}

val versionString: String = version as String
val isRelease: Boolean = !versionString.contains('-')

val suffixedVersion: String = if (isRelease) {
    versionString
} else {
    versionString + "+" + System.getenv("GITHUB_RUN_NUMBER")
}

val changelogContent: String = latestCommitMessage()

hangarPublish {
    publications.register("plugin") {
        version.set(suffixedVersion)
        channel.set(System.getenv("HANGAR_BUILD_CHANNEL") ?: "Snapshot")
        changelog.set(changelogContent)
        id.set(slug)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set((property("paperVersion") as String).split(",").map { it.trim() })
            }

            register(Platforms.VELOCITY) {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set((property("velocityVersion") as String).split(",").map { it.trim() })
            }
        }
    }
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf(
        "version" to project.version
    )
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.get().java.srcDir(generateTemplates.map { outputs -> outputs })
rootProject.idea.project.settings.taskTriggers.afterSync(generateTemplates)

/* First add a new source set. Don't use your main source set for generated stuff. */
sourceSets.main.get().java.srcDirs("src/generated/java", "src/main/java")

/* All other things will be set inside the nmsGen method, */
nmsGen {
    basePackage = "ir.syrent.nms.accessors" // All generated classes will be in this package.
    sourceSet = "src/generated/java" // All generated classes will be part of this source set.
    minMinecraftVersion = "1.8"

    /* This means that the folder will be cleared before generation.
     *
     * If this value is false, old and no longer used classes won't be removed.
     */
    isCleanOnRebuild = true

    /* Here we can define the classes */
    val ServerGamePacketListenerImpl = reqClass("net.minecraft.server.network.ServerGamePacketListenerImpl")
    val ClientboundUpdateMobEffectPacket = reqClass("net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket")
    val ClientboundRemoveMobEffectPacket = reqClass("net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket")
    val ClientboundPlayerInfoUpdatePacket = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket")
    val ClientboundPlayerInfoUpdatePacketAction = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket\$Action")
    val ClientboundPlayerInfoUpdatePacketEntry = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket\$Entry")
    val ClientboundPlayerInfoRemovePacket = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket")

    val ServerPlayer = reqClass("net.minecraft.server.level.ServerPlayer")
    val Entity = reqClass("net.minecraft.world.entity.Entity")
    val GameType = reqClass("net.minecraft.world.level.GameType")
    val Component = reqClass("net.minecraft.network.chat.Component")
    val RemoteChatSessionData = reqClass("net.minecraft.network.chat.RemoteChatSession\$Data")
    val Packet = reqClass("net.minecraft.network.protocol.Packet")
    val Connection = reqClass("net.minecraft.network.Connection")


    val MobEffect = reqClass("net.minecraft.world.effect.MobEffect")
    val MobEffectInstance = reqClass("net.minecraft.world.effect.MobEffectInstance")

    Connection
        .reqField("channel")

    ServerPlayer
        .reqField("connection")
    Entity
        .reqMethod("getUUID")
    GameType
        .reqEnumField("SURVIVAL")
        .reqEnumField("CREATIVE")
        .reqEnumField("SPECTATOR")
        .reqEnumField("ADVENTURE")
        .reqMethod("byName", String::class)
    MobEffect
        .reqMethod("byId", Int::class)
    MobEffectInstance
        .reqConstructor(MobEffect, Int::class, Int::class, Boolean::class, Boolean::class, Boolean::class)
        .reqField("effect")

    ServerGamePacketListenerImpl
        .reqMethod("send", Packet)
        .reqField("connection")
    ClientboundUpdateMobEffectPacket
        .reqConstructor(Int::class, MobEffectInstance)
    ClientboundRemoveMobEffectPacket
        .reqConstructor(Int::class, MobEffect)
    ClientboundPlayerInfoUpdatePacket
        .reqConstructor(ClientboundPlayerInfoUpdatePacketAction, ServerPlayer)
        .reqMethod("createPlayerInitializing", Collection::class)
        .reqField("entries")
        .reqMethod("entries")
    ClientboundPlayerInfoUpdatePacketAction
        .reqEnumField("UPDATE_GAME_MODE")
        .reqEnumField("ADD_PLAYER")
    ClientboundPlayerInfoUpdatePacketEntry
        .reqConstructor(UUID::class, "com.mojang.authlib.GameProfile", Boolean::class, Int::class, GameType, Component, RemoteChatSessionData)
    ClientboundPlayerInfoRemovePacket
        .reqConstructor(List::class)
        .reqField("profileIds")
}