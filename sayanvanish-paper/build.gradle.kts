import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.sayandev.plugin.StickyNoteModules
import xyz.jpenilla.runpaper.task.RunServer

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
        runDirectory = file("run/bukkit-1")
        jvmArgs("-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false", "-Dserver.id=lobby-1")
    }
    withType(RunServer::class.java) {
        minecraftVersion("1.21.11")
        pluginJars(shadowJar)

        downloadPlugins {
            hangar("ViaVersion", "5.6.1-SNAPSHOT+877")
            hangar("PlaceholderAPI", "2.11.7")
            modrinth("essentialsx", "2.21.2")
            modrinth("luckperms", "v5.5.17-bukkit")
            modrinth("tab-bridge", "6.2.0")
//            url("https://github.com/NEZNAMY/TAB/releases/download/5.4.0/TAB.v5.4.0.jar")
        }
        jvmArgs("-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false")
    }
    register<RunServer>("runServer2") {
        runDirectory = file("run/bukkit-2")
        jvmArgs("-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false", "-Dserver.id=lobby-2")
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

    loader = "${rootProject.group}.${findProperty("slug")!! as String}.paper.${rootProject.name}Loader"
    main = "${rootProject.group}.${findProperty("slug")!! as String}.paper.${rootProject.name}Plugin"

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