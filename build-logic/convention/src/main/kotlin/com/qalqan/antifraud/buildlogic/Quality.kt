package com.qalqan.antifraud.buildlogic

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

internal fun Project.applyQuality() {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension>("detekt") {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = false
        }
    }
}

internal fun Project.addJvmTestDependencies() {
    val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
    fun lib(alias: String) = libs.findLibrary(alias).get()
    dependencies {
        add("testImplementation", lib("junit-jupiter-api"))
        add("testImplementation", lib("junit-jupiter-params"))
        add("testRuntimeOnly", lib("junit-jupiter-engine"))
        add("testRuntimeOnly", lib("junit-vintage-engine"))
        add("testImplementation", lib("kotest-assertions-core"))
        add("testImplementation", lib("mockk"))
    }
}

internal fun Project.addAndroidTestDependencies() {
    val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
    fun lib(alias: String) = libs.findLibrary(alias).get()
    dependencies {
        add("testImplementation", lib("junit4"))
        add("testImplementation", lib("robolectric"))
        add("testImplementation", lib("androidx-test-core"))
        add("testImplementation", lib("androidx-test-ext-junit"))
        add("androidTestImplementation", lib("androidx-test-runner"))
        add("androidTestImplementation", lib("androidx-test-ext-junit"))
    }
}
