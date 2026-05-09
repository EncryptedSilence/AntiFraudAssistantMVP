plugins {
    id("antifraud.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.qalqan.antifraud.database"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.sqlcipher.android)
    implementation(libs.moshi)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)
}
