package org.sayandev.sayanvanish.paper

import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import org.sayandev.stickynote.loader.paper.StickyNotePaperLoader

class SayanVanishLoader : PluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        StickyNotePaperLoader(this, classpathBuilder)
            .load()
    }
}
