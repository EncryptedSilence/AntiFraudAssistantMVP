plugins {
    id("antifraud.android.library")
}

android {
    namespace = "com.qalqan.antifraud.demo"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(libs.coroutines.core)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
}
