plugins {
    id("antifraud.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.qalqan.antifraud.database"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.sqlcipher.android)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)
}
