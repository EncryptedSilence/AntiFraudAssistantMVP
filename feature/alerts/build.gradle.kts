plugins {
    id("antifraud.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.qalqan.antifraud.alerts"
    buildFeatures.compose = true
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:scoring"))
    implementation(project(":core:correlation"))
    implementation(project(":core:database"))
    implementation(project(":core:patterns"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
}
