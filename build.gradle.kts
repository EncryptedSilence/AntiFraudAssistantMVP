// Root build file. Conventions live in build-logic/.
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
