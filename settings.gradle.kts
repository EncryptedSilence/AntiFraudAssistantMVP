pluginManagement {
    includeBuild("build-logic")
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

rootProject.name = "antifraud-assistant"

include(
    ":app",
    ":core:domain",
    ":core:scoring",
    ":core:correlation",
    ":core:database",
    ":core:demo",
    ":core:patterns",
    ":core:crypto",
    ":feature:calls",
    ":feature:sms",
    ":feature:web",
)
