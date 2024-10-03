import org.sayandev.plugin.StickyNoteModules

allprojects {
    stickynote {
        modules(StickyNoteModules.PROXY)
    }

    dependencies {
        implementation(project(":sayanvanish-api"))
        stickynote.implementation("com.mysql:mysql-connector-j:8.4.0")
        stickynote.implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    }
}