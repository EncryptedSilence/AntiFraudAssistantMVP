// Root build file. Conventions live in build-logic/.

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("checkAll") {
    group = "verification"
    description = "Runs lint, ktlint, detekt, and unit tests across all modules."
    dependsOn(
        subprojects.map { it.tasks.matching { t -> t.name == "ktlintCheck" } },
        subprojects.map { it.tasks.matching { t -> t.name == "detekt" } },
        subprojects.map { it.tasks.matching { t -> t.name == "test" } },
        subprojects.map { it.tasks.matching { t -> t.name == "testDebugUnitTest" } }
    )
}
