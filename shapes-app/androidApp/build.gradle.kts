plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "io.approov.shapes.android"
    compileSdk = 33
    defaultConfig {
        applicationId = "io.approov.shapes.android"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // *** UNCOMMENT THE LINE BELOW FOR APPROOV ***
    //implementation("io.approov:service.okhttp:3.5.8")
}

kotlin {
    jvmToolchain(17)
}
