plugins {
    id("antifraud.android.library")
}

android {
    namespace = "com.qalqan.antifraud.settings"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:scoring"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
