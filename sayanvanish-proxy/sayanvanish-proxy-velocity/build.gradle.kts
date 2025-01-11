import org.sayandev.plugin.StickyNoteModules

plugins {
    id("xyz.jpenilla.run-velocity") version "2.3.0"
}

stickynote {
    modules(StickyNoteModules.VELOCITY)
}

dependencies {
    compileOnly(libs.velocity.api)
    compileOnly(libs.velocitab)
    compileOnly(libs.enhancedvelocity)
    annotationProcessor(libs.velocity.api)

    api(project(":sayanvanish-proxy"))
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-Dnet.kyori.adventure.text.warnWhenLegacyFormattingDetected=false")
}


tasks {
    runVelocity {
        velocityVersion("3.4.0-SNAPSHOT")

        downloadPlugins {
//            url("https://github.com/NEZNAMY/TAB/releases/download/5.0.3/TAB.v5.0.3.jar")
            url("https://download.luckperms.net/1560/velocity/LuckPerms-Velocity-5.4.145.jar")
//            url("https://cdn.modrinth.com/data/Q10irTG0/versions/eXh7ktan/Velocitab-1.7.2-67931d8.jar")
//            url("https://github.com/Syrent/EnhancedVelocity/releases/download/1.3.3/EnhancedVelocity.v1.3.3.jar")
            url("https://cdn.modrinth.com/data/HQyibRsN/versions/FfO1vuOg/MiniPlaceholders-Velocity-2.2.4.jar")
//            url("https://cdn.modrinth.com/data/asOrgO06/versions/2t7UMpJh/VelocityCoolList-2.0-SNAPSHOT.jar")
//            url("https://github.com/bivashy/MC-Auth-with-Link/releases/download/1.7.12/mcAuth-velocity-1.7.12.jar")
//            url("https://github.com/SkinsRestorer/SkinsRestorer/releases/download/15.4.3/SkinsRestorer.jar")
//            url("https://hangarcdn.papermc.io/plugins/Andre_601/AdvancedServerList/versions/4.11.1/VELOCITY/AdvancedServerList-Velocity-4.11.1.jar")
//            url("https://ci.lucko.me/job/spark/419/artifact/spark-velocity/build/libs/spark-1.10.74-velocity.jar")
        }
    }
}