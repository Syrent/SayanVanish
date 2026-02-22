import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.sayandev.plugin.StickyNoteModules

plugins {
    id("xyz.jpenilla.run-paper") version "3.0.2"
//    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("de.eldoria.plugin-yml.paper") version "0.8.0"
}

stickynote {
    modules(StickyNoteModules.PAPER, StickyNoteModules.BUKKIT, StickyNoteModules.BUKKIT_NMS)
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
    compileOnly(libs.miniplaceholders.api)

//    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
//    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation(project(":sayanvanish-api"))
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.AMAZON
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs(/*"-XX:+AllowEnhancedClassRedefinition", */"-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false")
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
        if (gradle.startParameter.taskNames.isNotEmpty() && (gradle.startParameter.taskNames.contains("runServer") || gradle.startParameter.taskNames.contains("runFolia"))) {
            toolchain.languageVersion = JavaLanguageVersion.of(21)
        }
    }

    runServer {
        minecraftVersion("1.21.11")

        downloadPlugins {
            hangar("ViaVersion", "5.6.1-SNAPSHOT+877")
            hangar("PlaceholderAPI", "2.11.7")
            modrinth("essentialsx", "2.21.2")
            modrinth("luckperms", "v5.5.17-bukkit")
            url("https://github.com/NEZNAMY/TAB/releases/download/5.4.0/TAB.v5.4.0.jar")
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
        jvmArgs("-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false")
    }

    runPaper {
        folia.registerTask()
    }

//    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

paper {
    name = rootProject.name
    version = rootProject.version as String
    description = rootProject.description
    website = findProperty("website")!! as String
    author = findProperty("author")!! as String
    defaultPermission = BukkitPluginDescription.Permission.Default.FALSE

    loader = "${rootProject.group}.${findProperty("slug")!! as String}.bukkit.${rootProject.name}Loader"
    main = "${rootProject.group}.${findProperty("slug")!! as String}.bukkit.${rootProject.name}Plugin"

    foliaSupported = true

    apiVersion = "1.20"

    permissions {
        register("sayanvanish.action.vanish.onjoin") {
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
        register("sayanvanish.feature.disable.*") {
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
    }

    serverDependencies {
        register("Essentials") {
            required = false
        }
        register("squaremap") {
            required = false
        }
        register("LuckPerms") {
            required = false
        }
        register("PlaceholderAPI") {
            required = false
        }
        register("MiniPlaceholders") {
            required = false
        }
        register("Citizens") {
            required = false
        }
        register("TAB") {
            required = false
        }
    }
}

modrinth {
    loaders.set(listOf(
        "spigot",
        "paper",
        "purpur",
        "folia"
    ))
}