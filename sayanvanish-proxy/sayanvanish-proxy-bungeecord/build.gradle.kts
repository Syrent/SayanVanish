repositories {
    // Bungeecord
    maven("https://oss.sonatype.org/content/repositories/snapshots")

    // libby
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.sayandev:stickynote-proxy-bungeecord:1.0.32")
//    compileOnly("com.mysql:mysql-connector-j:8.4.0")
//    compileOnly("org.xerial:sqlite-jdbc:3.46.0.0")

    implementation("com.alessiodp.libby:libby-bungee:2.0.0-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")

    implementation(project(":sayanvanish-proxy"))
}