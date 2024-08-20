import org.sayandev.*
import org.sayandev.applyShadowRelocation
import org.sayandev.plugin.StickyNoteModules

allprojects {
    stickynote {
        modules(StickyNoteModules.PROXY)
    }

    repositories {
        mavenCentral()
        applyRepositories(Module.VELOCITY)
        applyRepositories(Module.BUNGEECORD)
    }

    dependencies {
        implementation(project(":sayanvanish-api"))
        stickynote.implementation("com.mysql:mysql-connector-j:8.4.0")
        stickynote.implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    }
}

/*
subprojects {
    dependencies {
        compileOnlyApi(project(":sayanvanish-proxy"))
    }
}*/
