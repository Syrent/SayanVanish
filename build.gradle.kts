import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.hangarpublishplugin.model.Platforms
import org.sayandev.getRelocations
import org.sayandev.plugin.StickyNoteModules
import java.io.ByteArrayOutputStream
import java.net.URL
import java.net.HttpURLConnection
import com.google.gson.JsonParser

plugins {
    `java-library`
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.modrinth.minotaur") version "2.8.7"
    id("org.sayandev.stickynote.project")
}

val slug = findProperty("slug")!! as String
description = findProperty("description")!! as String

fun executeGitCommand(vararg command: String): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", *command)
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8).trim()
}

fun lastCommitMessages(): String {
    val url = URL("https://api.github.com/repos/Syrent/$name/actions/runs?status=success&per_page=1")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
    val response = connection.inputStream.bufferedReader().use { it.readText() }
    val sha = JsonParser.parseString(response).asJsonObject.getAsJsonArray("workflow_runs").get(0).asJsonObject.get("head_sha").asString

    return executeGitCommand("log", "--pretty=format:%s%n", "$sha..HEAD")
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

    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("kotlin")
    plugins.apply("org.sayandev.stickynote.project")
    plugins.apply("com.modrinth.minotaur")

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.sayandev.org/snapshots")
    }

    dependencies {
        stickynote.implementation("redis.clients:jedis:5.1.5")
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
        val modrinthApiKey = System.getenv("MODRINTH_API_TOKEN")
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
    java {
        withSourcesJar()

        disableAutoTargetJvm()
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

    artifacts.archives(tasks.shadowJar)

    tasks.named<Jar>("sourcesJar") {
        getRelocations().forEach { (from, to) ->
            val filePattern = Regex("(.*)${from.replace('.', '/')}((?:/|$).*)")
            val textPattern = Regex.fromLiteral(from)
            eachFile {
                filter {
                    it.replaceFirst(textPattern, to)
                }
                path = path.replaceFirst(filePattern, "$1${to.replace('.', '/')}$2")
            }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                shadow.component(this)
                artifact(tasks["sourcesJar"])
                this.version = versionString
                setPom(this)
            }
        }

        repositories {
            maven {
                name = "sayandevelopment-repo"
                url = uri("https://repo.sayandev.org/snapshots/")

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