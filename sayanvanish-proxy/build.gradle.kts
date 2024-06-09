import org.sayandev.*
import org.sayandev.applyShadowRelocation
import org.sayandev.plugin.StickyNoteModules

allprojects {
    stickynote {
        modules(StickyNoteModules.PROXY)
    }

    repositories {
        applyRepositories(Module.VELOCITY)
        applyRepositories(Module.BUNGEECORD)
    }

    dependencies {
        implementation(project(":sayanvanish-api"))
    }
}

/*
subprojects {
    dependencies {
        compileOnlyApi(project(":sayanvanish-proxy"))
    }
}*/
