import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.gson.JsonParser
import io.papermc.hangarpublishplugin.model.Platforms
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

plugins {
    java
    kotlin("jvm") version "2.1.0"
    `maven-publish`
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.modrinth.minotaur") version "2.8.7"
    id("org.sayandev.stickynote.project")
}

val slug = findProperty("slug")!! as String
description = findProperty("description")!! as String

fun executeGitCommand(vararg command: String): String {
    val processBuilder = ProcessBuilder("git", *command)
    val process = processBuilder.start()
    val byteOut = ByteArrayOutputStream()
    process.inputStream.copyTo(byteOut)
    process.waitFor()
    return byteOut.toString(Charsets.UTF_8).trim()
}

fun lastCommitMessages(): String {
    val url = URL("https://api.github.com/repos/Syrent/$name/actions/runs?status=success&per_page=1")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
    val response = connection.inputStream.bufferedReader().use { it.readText() }
    val sha = JsonParser.parseString(response).asJsonObject.getAsJsonArray("workflow_runs").get(0).asJsonObject.get("head_sha").asString

    return executeGitCommand("log", "--pretty=format:\"%C(auto)%h %s %C(blue)<%an>\"", "$sha..HEAD")
}

fun lastReleaseCommitMessages(): String {
    val url = URL("https://api.github.com/repos/Syrent/$name/releases")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
    val response = connection.inputStream.bufferedReader().use { it.readText() }
    val previousReleaseVersion = JsonParser.parseString(response).asJsonArray.get(1).asJsonObject.get("tag_name").asString

    val currentProjectVersion = versionString

    return executeGitCommand("log", "--pretty=format:%s%n", "$previousReleaseVersion..$currentProjectVersion")
}

val versionString: String = findProperty("version")!! as String
val isRelease: Boolean = (System.getenv("HANGAR_BUILD_CHANNEL") ?: "Snapshot") == "Release"

val publishVersion = if (isRelease) versionString else "$versionString-build.${System.getenv("GITHUB_RUN_NUMBER") ?: "dev"}"
val commitVersion = publishVersion + "-" + (System.getenv("GITHUB_SHA")?.substring(0, 7) ?: "local")
version = commitVersion

val changelogContent: String = if (isRelease) lastReleaseCommitMessages() else lastCommitMessages()

tasks {
    publishAllPublicationsToHangar {
        this.dependsOn(shadowJar)
        this.mustRunAfter(shadowJar)
    }
}

allprojects {
    group = findProperty("group")!! as String
    version = findProperty("version")!! as String

    plugins.apply("java")
    plugins.apply("maven-publish")
    plugins.apply("kotlin")
    plugins.apply("org.sayandev.stickynote.project")
    plugins.apply("com.modrinth.minotaur")

    repositories {
        mavenLocal()
        mavenCentral()

        maven {
            name = "sayandevelopment-snapshots"
            url = uri("https://repo.sayandev.org/snapshots")
        }

        maven {
            name = "sayandevelopment-releases"
            url = uri("https://repo.sayandev.org/releases")
        }

        maven {
            name = "velocitab"
            url = uri("https://repo.william278.net/releases")
        }

        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }

        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            name = "extendedclip"
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }

        maven {
            name = "citizens"
            url = uri("https://repo.citizensnpcs.co/")
        }

        maven {
            name = "essentialsx"
            url = uri("https://repo.essentialsx.net/releases/")
        }

        maven {
            name = "snatype-snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }

        maven {
            name = "codeberg"
            url = uri("https://codeberg.org/api/packages/Andre601/maven/")
        }

        maven {
            name = "scarsz"
            url = uri("https://nexus.scarsz.me/content/groups/public/")
        }

        maven {
            name = "modrinth-repo"
            url = uri("https://api.modrinth.com/maven/")
        }
    }

    tasks {
        processResources {
            filesMatching(listOf("**plugin.yml", "**plugin.json")) {
                expand(
                    "version" to commitVersion,
                    "slug" to slug,
                    "name" to rootProject.name,
                    "description" to rootProject.description
                )
            }
        }
    }

    modrinth {
        val modrinthApiKey = System.getenv("MODRINTH_API_TOKEN") ?: project.findProperty("modrinth.api.token") as String
        val modrinthChangelog = if (System.getenv("MODRINTH_CHANGELOG").isNullOrEmpty()) changelogContent else System.getenv("MODRINTH_CHANGELOG")

        token.set(modrinthApiKey)
        projectId.set("${rootProject.property("modrinthProjectID")}")

        versionNumber.set(if (isRelease) versionString else publishVersion.replace("-build.", "-b").replace("-SNAPSHOT", ""))
        versionType.set(System.getenv("MODRINTH_BUILD_CHANNEL") ?: "beta")
        uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
        gameVersions.set("${rootProject.property("modrinthMinecraftVersions")}".split(","))
        failSilently.set(true)
        detectLoaders.set(true)

        changelog.set(modrinthChangelog)

        syncBodyFrom.set(rootProject.file("README.md").readText())
    }
}

subprojects {
    configurations {
        create("compileOnlyApiResolved") {
            isCanBeResolved = true
            extendsFrom(configurations.getByName("compileOnlyApi"))
        }
    }

    java {
        withSourcesJar()

        disableAutoTargetJvm()
    }

    val publicationShadowJar by tasks.registering(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        from(sourceSets.main.get().output)
        configurations = listOf(*configurations.toTypedArray(), this@subprojects.configurations["compileOnlyApiResolved"])
        archiveClassifier.set("")
    }

    tasks {
        jar {
            archiveClassifier.set("unshaded")
        }

        build {
            dependsOn(shadowJar)
        }

        withType<ShadowJar> {
            archiveFileName.set("${rootProject.name}-${commitVersion}-${this@subprojects.name.removePrefix("sayanvanish-")}.jar")
            archiveClassifier.set(null as String?)
            destinationDirectory.set(file(rootProject.projectDir.path + "/bin"))
            from("LICENSE")
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
//                from(components["shadow"])
                artifact(publicationShadowJar.get())
                artifact(tasks["sourcesJar"])
                this.version = versionString

                setPom(this)
            }
        }

        repositories {
            maven {
                name = "sayandevelopment-repo"
                url = uri(if (versionString.contains("-SNAPSHOT")) "https://repo.sayandev.org/snapshots/" else "https://repo.sayandev.org/releases/")

                credentials {
                    username = System.getenv("REPO_SAYAN_USER") ?: project.findProperty("repo.sayan.user") as? String
                    password = System.getenv("REPO_SAYAN_TOKEN") ?: project.findProperty("repo.sayan.token") as? String
                }
            }
        }
    }
}

fun setPom(publication: MavenPublication) {
    publication.pom {
        name.set("sayanvanish")
        description.set(project.description)
        url.set("https://github.com/syrent/sayanvanish")
        licenses {
            license {
                name.set("GNU General Public License v3.0")
                url.set("https://github.com/syrent/sayanvanish/blob/master/LICENSE")
            }
        }
        developers {
            developer {
                id.set("syrent")
                name.set("abbas")
                email.set("syrent2356@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/syrent/sayanvanish.git")
            developerConnection.set("scm:git:ssh://github.com/syrent/sayanvanish.git")
            url.set("https://github.com/syrent/sayanvanish/tree/master")
        }
    }
}

hangarPublish {
    publications.register("plugin") {
        version.set(if (isRelease) versionString else publishVersion)
        channel.set(System.getenv("HANGAR_BUILD_CHANNEL") ?: "Snapshot")
        changelog.set(if (System.getenv("HANGAR_CHANGELOG").isNullOrEmpty()) changelogContent else System.getenv("HANGAR_CHANGELOG"))
        id.set(slug)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        platforms {
            register(Platforms.PAPER) {
                jar.set(project(":sayanvanish-bukkit").tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set((property("paperVersion") as String).split(",").map { it.trim() })
            }

            register(Platforms.VELOCITY) {
                jar.set(project(":sayanvanish-proxy:sayanvanish-proxy-velocity").tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set((property("velocityVersion") as String).split(",").map { it.trim() })
            }

            register(Platforms.WATERFALL) {
                jar.set(project(":sayanvanish-proxy:sayanvanish-proxy-bungeecord").tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set((property("waterfallVersion") as String).split(",").map { it.trim() })
            }
        }
    }
}