plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "de.klyk.annotationprocessorexcel"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.klyk.annotationprocessorexcel"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    kotlin {
        sourceSets {
            getByName("debug") {
                kotlin.srcDirs("build/generated/ksp/debug/kotlin")
            }
            getByName("release") {
                kotlin.srcDirs("build/generated/ksp/release/kotlin")
            }
        }
    }
    // KSP Parameter, ob der Prozessor ausgeführt werden soll: ./gradlew build -PrunProcessor=true
    ksp {
        arg("runProcessor", providers.gradleProperty("runProcessor").orElse("false"))
    }
}

dependencies {
    implementation(project(":processor")) // since you want to use your @Listed annotation
    ksp(project(":processor")) // since you want to use your @Listed annotation
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