import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "de.klyk.feature"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    testOptions.targetSdk = 36
    lint.targetSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    android.sourceSets.named("debug") {
        kotlin.directories += "build/generated/ksp/debug/kotlin"
    }

    android.sourceSets.named("release") {
        kotlin.directories += "build/generated/ksp/release/kotlin"
    }

    ksp {
        arg("runDSGVOProcessor", providers.gradleProperty("runDSGVOProcessor").orElse("true"))
        arg("project.root", projectDir.parent.toString())
    }
}

dependencies {
    implementation(project(":processor")) // since you want to use your @ annotations
    ksp(project(":processor"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
