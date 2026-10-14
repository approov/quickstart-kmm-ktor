plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("8.13.2").apply(false)
    id("com.android.library").version("8.13.2").apply(false)
    id("org.jetbrains.kotlin.plugin.serialization").version("2.2.21").apply(false)
    kotlin("android").version("2.2.21").apply(false)
    kotlin("multiplatform").version("2.2.21").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
