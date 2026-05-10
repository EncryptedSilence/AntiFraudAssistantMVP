plugins {
    id("antifraud.kotlin.jvm")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:scoring"))
    implementation(project(":core:patterns"))
}
