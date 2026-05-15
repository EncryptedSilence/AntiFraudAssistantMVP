package com.qalqan.antifraud.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        val appVersion = resolveAppVersion()
        extensions.extraProperties["antifraudVersionName"] = appVersion.name
        extensions.extraProperties["antifraudVersionCode"] = appVersion.code

        extensions.configure<ApplicationExtension>("android") {
            compileSdk = 34
            defaultConfig {
                minSdk = 26
                targetSdk = 34
                versionCode = appVersion.code
                versionName = appVersion.name
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
                targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
            }
        }

        tasks.withType(KotlinCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
            compilerOptions.allWarningsAsErrors.set(true)
        }

        applyQuality()
        addJvmTestDependencies()
    }
}
