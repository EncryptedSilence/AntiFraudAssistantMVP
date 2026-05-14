plugins {
    id("antifraud.android.application")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.qalqan.antifraud"
    buildFeatures.compose = true
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                if (test.name.contains("Release")) {
                    test.exclude("**/ExportSheetGatingTest.class")
                    test.exclude("**/AntifraudAppSmokeTest.class")
                    test.exclude("**/ui/state/**")
                    test.exclude("**/ui/home/**")
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:scoring"))
    implementation(project(":core:correlation"))
    implementation(project(":core:database"))
    implementation(project(":core:demo"))
    implementation(project(":core:patterns"))
    implementation(project(":feature:calls"))
    implementation(project(":feature:sms"))
    implementation(project(":feature:web"))
    implementation(project(":core:crypto"))
    implementation(project(":core:sync"))
    implementation(project(":feature:export"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.coroutines.test)

    testImplementation(platform(libs.compose.bom))
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
