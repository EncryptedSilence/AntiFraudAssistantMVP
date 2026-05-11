plugins {
    id("antifraud.android.library")
}

android {
    namespace = "com.qalqan.antifraud.sms"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:scoring"))
    implementation(project(":core:correlation"))
    implementation(project(":core:database"))
    implementation(project(":feature:calls"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
