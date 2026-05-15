import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("antifraud.android.application")
    alias(libs.plugins.kotlin.compose)
}

(extensions.getByName("android") as AppExtension).applicationVariants.all {
    val capturedVariant = this
    outputs.all {
        (this as BaseVariantOutputImpl).outputFileName =
            "AntiFraud-${capturedVariant.versionName}-${capturedVariant.flavorName}.apk"
    }
}

android {
    namespace = "com.qalqan.antifraud"
    buildFeatures.compose = true

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            isDefault = true
        }
        create("qa") {
            dimension = "env"
        }
        create("prod") {
            dimension = "env"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                if (test.name.contains("Release")) {
                    test.exclude("**/ExportSheetGatingTest.class")
                    test.exclude("**/AntifraudAppSmokeTest.class")
                    test.exclude("**/AntifraudAppFirstLaunchRoutingTest.class")
                    test.exclude("**/ui/state/**")
                    test.exclude("**/ui/home/**")
                    test.exclude("**/ui/campaign/**")
                    test.exclude("**/ui/patterns/**")
                    test.exclude("**/ui/references/**")
                    test.exclude("**/ui/privacy/**")
                    test.exclude("**/ui/settings/**")
                    test.exclude("**/ui/onboarding/**")
                    test.exclude("**/ui/question/**")
                    test.exclude("**/ui/pause/**")
                    test.exclude("**/ui/education/**")
                    // Stage 8 acceptance tests that drive Compose surfaces.
                    test.exclude("**/acceptance/Acceptance2NoRegistrationOnFirstLaunchTest.class")
                    test.exclude("**/acceptance/Acceptance17ExplainabilityRendersAtLeastThreeReasonsTest.class")
                    test.exclude("**/acceptance/Acceptance44WizardGatingTest.class")
                    test.exclude("**/acceptance/Acceptance45ManualEntryReachableHomeTest.class")
                    test.exclude("**/acceptance/PauseBeforeActionAtCriticalTest.class")
                    test.exclude("**/acceptance/Stage8AcceptanceSuite.class")
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
    implementation(project(":feature:alerts"))
    implementation(project(":feature:settings"))

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
