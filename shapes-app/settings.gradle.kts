pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }

        // UNCOMMENT LINES BELOW TO USE APPROOV
        //maven {
        //    url = uri("https://jitpack.io")
        //}
    }
}

rootProject.name = "ApproovShapesApp"
include(":androidApp")
include(":shared")