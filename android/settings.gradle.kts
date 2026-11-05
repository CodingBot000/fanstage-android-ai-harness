pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FanStage"
include(":app", ":core:model", ":core:data", ":core:artwork", ":core:designsystem", ":feature:live", ":feature:discover", ":feature:community", ":feature:media", ":feature:messages", ":macrobenchmark")
