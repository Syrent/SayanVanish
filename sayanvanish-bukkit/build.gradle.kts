import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.sayandev.*
import org.sayandev.plugin.StickyNoteModules

plugins {
    id("xyz.jpenilla.run-paper") version "2.3.0"
//    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

stickynote {
    modules(StickyNoteModules.BUKKIT, StickyNoteModules.BUKKIT_NMS)
//    relocate(!gradle.startParameter.getTaskNames().any { it.startsWith("runServer") || it.startsWith("runFolia") || it.startsWith("runVelocity") })
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.folia.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.essentialsx)
    compileOnly(libs.citizens.api)
    compileOnly(libs.squaremap.api)
    compileOnly(libs.pl3xmap.api)
    compileOnly(libs.dynmap.core.api)
    compileOnly(libs.dynmap.api)

//    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
//    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation(project(":sayanvanish-api"))
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-Dorg.sayandev.sayanventure.adventure.text.warnWhenLegacyFormattingDetected=false")
}

tasks {
    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }

    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }

    java {
        if (gradle.startParameter.getTaskNames().isNotEmpty() && (gradle.startParameter.getTaskNames().contains("runServer") || gradle.startParameter.getTaskNames().contains("runFolia"))) {
            toolchain.languageVersion = JavaLanguageVersion.of(21)
        }
    }

    runServer {
        minecraftVersion("1.21.8")

        downloadPlugins {
            hangar("ViaVersion", "5.4.2-SNAPSHOT+776")
            hangar("PlaceholderAPI", "2.11.6")
            modrinth("essentialsx", "2.21.1")
            url("https://download.luckperms.net/1594/bukkit/loader/LuckPerms-Bukkit-5.5.9.jar")
//            hangar("AdvancedServerList", "5.4.1")
//            url("https://cdn.modrinth.com/data/qvdtDX3s/versions/TD9kTO2n/multiverse-inventories-4.2.7-pre.jar")
//            url("https://github.com/SkinsRestorer/SkinsRestorer/releases/download/15.5.1/SkinsRestorer.jar")
//            url("https://cdn.modrinth.com/data/3wmN97b8/versions/ehwU9G3y/multiverse-core-4.3.15-pre.2.jar")
//            url("https://cdn.modrinth.com/data/16vhQOQN/versions/g55TGTXG/minimotd-bukkit-2.1.4.jar")
//            url("https://github.com/NEZNAMY/TAB/releases/download/4.1.6/TAB.v4.1.6.jar")
//            url("https://ci.lucko.me/job/spark/418/artifact/spark-bukkit/build/libs/spark-1.10.73-bukkit.jar")
//            url("https://github.com/Insprill/custom-join-messages/releases/download/v17.5.0/custom-join-messages-17.5.0.jar")
//            url("https://github.com/NEZNAMY/TAB-Bridge/releases/download/6.0.1/TAB-Bridge.v6.0.1.jar")
//            url("https://download.luckperms.net/1593/bukkit/loader/LuckPerms-Bukkit-5.5.8.jar")
        }
        jvmArgs("-Dorg.sayandev.sayanventure.adventure.text.warnWhenLegacyFormattingDetected=false")
    }

    runPaper {
        folia.registerTask()
    }

//    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

bukkit {
    name = rootProject.name
    version = rootProject.version as String
    description = rootProject.description
    website = findProperty("website")!! as String
    author = findProperty("author")!! as String
    defaultPermission = BukkitPluginDescription.Permission.Default.FALSE

    main = "${rootProject.group}.${findProperty("slug")!! as String}.bukkit.${rootProject.name}"

    foliaSupported = true

    apiVersion = "1.13"

    permissions {
        register("sayanvanish.action.vanish.onjoin") {
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
        register("sayanvanish.feature.disable.*") {
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
    }

    softDepend = listOf(
        "Essentials",
        "squaremap",
        "LuckPerms",
        "PlaceholderAPI",
        "MiniPlaceholders",
        "Citizens",
        // just to fix adventure problem on 1.8
        "packetevents",
        "TAB"
    )
}

modrinth {
    loaders.set(listOf(
        "spigot",
        "paper",
        "purpur",
        "folia"
    ))
}