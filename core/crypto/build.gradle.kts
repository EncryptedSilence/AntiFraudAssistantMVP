plugins {
    id("antifraud.android.library")
}

android {
    namespace = "com.qalqan.antifraud.crypto"
}

dependencies {
    implementation(libs.tink.android)
}
