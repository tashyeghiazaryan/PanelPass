plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget {
        publishLibraryVariants("debug", "release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }
        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

android {
    namespace = "com.panelpass.shared"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
