plugins {
    id("antifraud.android.library")
}

android {
    namespace = "com.qalqan.antifraud.sync"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:crypto"))
    implementation(project(":core:patterns"))
    implementation(project(":core:database"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    testImplementation(libs.tink.android)
    testImplementation(libs.coroutines.test)
}
