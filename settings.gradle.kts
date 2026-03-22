rootProject.name = "PanelPass"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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
