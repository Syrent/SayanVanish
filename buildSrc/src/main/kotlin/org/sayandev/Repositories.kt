package org.sayandev

val paperApi = Dependency(
    group = "io.papermc.paper",
    artifact = "paper-api",
    version = "1.20.6-R0.1-SNAPSHOT",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.BUKKIT)
)

val foliaAPI = Dependency(
    group = "dev.folia",
    artifact = "folia-api",
    version = "1.20.4-R0.1-SNAPSHOT",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.BUKKIT)
)

val velocityAPI = Dependency(
    group = "com.velocitypowered",
    artifact = "velocity-api",
    version = "3.3.0-SNAPSHOT",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY_API,
    modules = listOf(Module.VELOCITY)
)

val velocityAPIAnnotation = Dependency(
    group = "com.velocitypowered",
    artifact = "velocity-api",
    version = "3.3.0-SNAPSHOT",
    relocation = null,
    type = Dependency.Type.ANNOTATION_PROCESSOR,
    modules = listOf(Module.VELOCITY)
)

val bungeecordAPI = Dependency(
    group = "net.md-5",
    artifact = "bungeecord-api",
    version = "1.19-R0.1-SNAPSHOT",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.BUNGEECORD)
)

val advancedServerList = Dependency(
    group = "ch.andre601.asl-api",
    artifact = "api",
    version = "3.3.0",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.BUKKIT, Module.VELOCITY)
)

val essentialsX = Dependency(
    group = "net.essentialsx",
    artifact = "EssentialsX",
    version = "2.20.1",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.BUKKIT)
)

val citizens = Dependency(
    group = "net.citizensnpcs",
    artifact = "citizensapi",
    version = "2.0.28-SNAPSHOT",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.BUKKIT)
)

val squaremap = Dependency(
    group = "xyz.jpenilla",
    artifact = "squaremap-api",
    version = "1.2.3",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.BUKKIT)
)

val placeholderAPI = Dependency(
    group = "me.clip",
    artifact = "placeholderapi",
    version = "2.11.6",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.BUKKIT)
)

val enhancedVelocity = Dependency(
    group = "ir.syrent",
    artifact = "EnhancedVelocity",
    version = "1.3.3",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.VELOCITY)
)

val velocitab = Dependency(
    group = "net.william278",
    artifact = "velocitab",
    version = "1.6.5",
    relocation = null,
    type = Dependency.Type.COMPILE_ONLY,
    modules = listOf(Module.VELOCITY)
)

val repositories = listOf(
    Repository(
        id = "sayandevelopment",
        repos = listOf(
            "https://repo.sayandev.org/snapshots",
            "https://repo.sayandev.org/releases"
        ),
        dependencies = listOf(
            enhancedVelocity
        )
    ),
    Repository(
        id = "velocitab",
        repos = listOf("https://repo.william278.net/releases"),
        dependencies = listOf(
            velocitab
        )
    ),
    Repository(
        id = "papermc",
        repos = listOf("https://repo.papermc.io/repository/maven-public/"),
        dependencies = listOf(
            paperApi,
            foliaAPI,
            velocityAPI,
            velocityAPIAnnotation,
        )
    ),
    Repository(
        id = "extendedclip",
        repos = listOf("https://repo.extendedclip.com/content/repositories/placeholderapi/"),
        dependencies = listOf(
            placeholderAPI
        )
    ),
    Repository(
        id = "citizens",
        repos = listOf("https://repo.citizensnpcs.co/"),
        dependencies = listOf(
            citizens
        )
    ),
    Repository(
        id = "essentialsx",
        repos = listOf("https://repo.essentialsx.net/releases/"),
        dependencies = listOf(
            essentialsX
        ),
    ),
    Repository(
        id = "sonatype-snapshots",
        repos = listOf("https://oss.sonatype.org/content/repositories/snapshots"),
        dependencies = listOf(
            bungeecordAPI,
        )
    ),
    Repository(
        id = "codeberg",
        repos = listOf("https://codeberg.org/api/packages/Andre601/maven/"),
        dependencies = listOf(
            advancedServerList,
        )
    ),
    Repository(
        id = "central",
        repos = emptyList(),
        dependencies = listOf(
            squaremap,
        )
    ),
)