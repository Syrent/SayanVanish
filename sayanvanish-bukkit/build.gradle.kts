plugins {
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

repositories {
    // Spigot
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

    // cloud
    maven("https://oss.sonatype.org/content/repositories/snapshots")

    // MockBukkit
    maven("https://repo.papermc.io/repository/maven-public/")

    // Brigadier
    maven("https://libraries.minecraft.net")

    // Paper
    maven("https://repo.papermc.io/repository/maven-public/")

    // EssentialsX
    maven("https://repo.essentialsx.net/releases/")

    // libby
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")

    // Citizens
    maven("https://repo.citizensnpcs.co/")

    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    // dynmap
    maven("https://repo.mikeprimm.com/")
}

dependencies {
//    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

    compileOnly("org.sayandev:stickynote-core:1.0.31")
    compileOnly("org.sayandev:stickynote-bukkit:1.0.31")
    compileOnly("org.sayandev:stickynote-bukkit-nms:1.0.31")
    // dynmap api doesn't work
//    compileOnly("us.dynmap:dynmap-api:3.4-beta-3")
    implementation("com.alessiodp.libby:libby-bukkit:2.0.0-SNAPSHOT")

    implementation(project(":sayanvanish-api"))
//    api(kotlin("reflect"))

    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("xyz.jpenilla:squaremap-api:1.2.3")
    compileOnly("net.citizensnpcs:citizensapi:2.0.28-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.9.0")
    testImplementation("com.mojang:brigadier:1.0.18")
}

tasks {
    runServer {
        minecraftVersion("1.20.4")

        downloadPlugins {
            url("https://download.luckperms.net/1539/bukkit/loader/LuckPerms-Bukkit-5.4.126.jar")
            url("https://github.com/Insprill/custom-join-messages/releases/download/v17.5.0/custom-join-messages-17.5.0.jar")
            url("https://github.com/EssentialsX/Essentials/releases/download/2.20.1/EssentialsX-2.20.1.jar")
        }
    }
}
