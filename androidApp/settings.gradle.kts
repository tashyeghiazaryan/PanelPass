rootProject.name = "androidApp"

// Dependency resolution management for version catalogs
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Include the shared module if it exists, or comment it out if not yet created
// include(":shared")
