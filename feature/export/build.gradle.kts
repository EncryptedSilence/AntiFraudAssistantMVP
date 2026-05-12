plugins {
    id("antifraud.android.library")
}

android {
    namespace = "com.qalqan.antifraud.export"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:patterns"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
}
