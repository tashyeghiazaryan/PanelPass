rootProject.name = "PanelPass"

pluginManagement {
    // AGP публикуется в Google Maven, не в Gradle Plugin Portal — без google() будет ошибка резолва.
    repositories {
        google()
        maven {
            name = "GoogleAndroidMaven"
            url = uri("https://dl.google.com/dl/android/maven2/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application",
                "com.android.library",
                -> useModule("com.android.tools.build:gradle:${requested.version}")
                else -> Unit
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":shared")
include(":androidApp")

// Пример отдельного KMP-модуля фичи (раскомментируйте при выносе кода):
// include(":feature-profile")
