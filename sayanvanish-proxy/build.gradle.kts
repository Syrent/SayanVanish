import org.sayandev.plugin.StickyNoteModules

allprojects {
    stickynote {
        modules(StickyNoteModules.PROXY)
    }

    dependencies {
        implementation(project(":sayanvanish-api"))
    }
}