plugins {
    id("antifraud.android.application")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.qalqan.antifraud"
    buildFeatures.compose = true
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:scoring"))
    implementation(project(":core:correlation"))
    implementation(project(":core:database"))
    implementation(project(":core:demo"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
