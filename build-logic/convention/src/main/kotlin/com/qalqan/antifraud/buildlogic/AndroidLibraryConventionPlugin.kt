package com.qalqan.antifraud.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension>("android") {
            compileSdk = 34
            defaultConfig {
                minSdk = 26
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
                targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
        }

        tasks.withType(KotlinCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
            compilerOptions.allWarningsAsErrors.set(true)
        }

        applyQuality()

        val libs = extensions.getByName("libs") as VersionCatalog
        fun lib(alias: String) = libs.findLibrary(alias).get()
        dependencies {
            add("testImplementation", lib("junit-jupiter-api"))
            add("testImplementation", lib("junit-jupiter-params"))
            add("testRuntimeOnly", lib("junit-jupiter-engine"))
            add("testImplementation", lib("kotest-assertions-core"))
            add("testImplementation", lib("mockk"))
            add("testImplementation", lib("robolectric"))
            add("testImplementation", lib("androidx-test-core"))
            add("testImplementation", lib("androidx-test-ext-junit"))
            add("androidTestImplementation", lib("androidx-test-runner"))
            add("androidTestImplementation", lib("androidx-test-ext-junit"))
        }
    }
}
