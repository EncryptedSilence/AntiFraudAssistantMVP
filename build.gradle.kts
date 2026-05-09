// Root build file. Conventions live in build-logic/.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("checkAll") {
    group = "verification"
    description = "Runs ktlint, detekt, and unit tests across all modules."
    dependsOn(
        subprojects.map { it.tasks.matching { t -> t.name == "ktlintCheck" } },
        subprojects.map { it.tasks.matching { t -> t.name == "detekt" } },
        subprojects.map { it.tasks.matching { t -> t.name == "test" } },
        subprojects.map { it.tasks.matching { t -> t.name == "testDebugUnitTest" } }
    )
}
