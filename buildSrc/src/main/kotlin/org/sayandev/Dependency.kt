package org.sayandev

data class Dependency(
    val group: String,
    val artifact: String,
    val version: String,
    val relocation: Relocation? = null,
    val type: Type,
    val modules: List<Module>,
    val shadeMethod: ShadeMethod = ShadeMethod.DEFAULT,
) {
    constructor(dependency: String, relocation: Relocation?, type: Type, modules: List<Module>) : this(
        dependency.substringBefore(':'),
        dependency.substringAfter(':').substringBefore(':'),
        dependency.substringAfterLast(':'),
        relocation,
        type,
        modules
    )

    enum class Type(val configurationName: String) {
        COMPILE_ONLY("compileOnly"),
        IMPLEMENTATION("implementation"),
        TEST_IMPLEMENTATION("testImplementation"),
        API("api"),
        COMPILE_ONLY_API("compileOnlyApi"),
        ANNOTATION_PROCESSOR("annotationProcessor"),
    }

    enum class ShadeMethod {
        DEFAULT,
        FORCE,
        EXCLUDE,
    }
}