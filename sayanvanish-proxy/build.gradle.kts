import org.sayandev.*
import org.sayandev.applyShadowRelocation

dependencies {
    implementation(kotlin("stdlib", version = "2.0.0"))
}

allprojects {
    repositories {
        applyRepositories(Module.VELOCITY)
        applyRepositories(Module.BUNGEECORD)
    }

    dependencies {
        implementation(project(":sayanvanish-api"))
    }
}

subprojects {
    dependencies {
        compileOnlyApi(project(":sayanvanish-proxy"))
    }
}