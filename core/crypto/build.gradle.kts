plugins {
    id("antifraud.android.library")
}

android {
    namespace = "com.qalqan.antifraud.crypto"
}

dependencies {
    implementation(libs.tink.android)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
}
