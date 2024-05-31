dependencies {
    api(project(":sayanvanish-api"))
    compileOnlyApi("org.sayandev:stickynote-core:1.0.31")

    implementation(kotlin("stdlib", version = "2.0.0"))
    implementation(project(":sayanvanish-api"))
}