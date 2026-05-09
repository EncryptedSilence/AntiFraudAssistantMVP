plugins {
    `kotlin-dsl`
}

group = "com.qalqan.antifraud.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kotlinJvmConvention") {
            id = "antifraud.kotlin.jvm"
            implementationClass = "com.qalqan.antifraud.buildlogic.KotlinJvmConventionPlugin"
        }
        register("androidLibraryConvention") {
            id = "antifraud.android.library"
            implementationClass = "com.qalqan.antifraud.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidApplicationConvention") {
            id = "antifraud.android.application"
            implementationClass = "com.qalqan.antifraud.buildlogic.AndroidApplicationConventionPlugin"
        }
    }
}
