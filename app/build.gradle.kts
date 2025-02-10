plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "de.klyk.annotationprocessorexcel"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.klyk.annotationprocessorexcel"
        minSdk = 30
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint.targetSdk = 35

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
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
    // KSP Parameter, ob der Prozessor ausgeführt werden soll: ./gradlew build -PrunDSGVOProcessor=true
    ksp {
        arg("runDSGVOProcessor", providers.gradleProperty("runDSGVOProcessor").orElse("true"))
        arg("exportDSGVOExcel", "true")
        arg("project.root", projectDir.parent.toString())
    }
}

dependencies {
    implementation(project(":processor")) // since you want to use your @ annotations
    ksp(project(":processor"))
    implementation((project(":feature")))

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

tasks.register("clearDSGVODataStoreFiles") {
    dependsOn(":app:kspDebugKotlin")
    doLast("CleaningDSGVOBuffer") {
        val fileExcelBuffer = file("${project.rootDir}/build/ksp-exports/dsgvo_data.json")
        val fileCSVBuffer = file("${project.rootDir}/build/ksp-exports/dsgvo_data.csv")

        if (fileExcelBuffer.exists()) fileExcelBuffer.delete()
        if (fileCSVBuffer.exists()) fileCSVBuffer.delete()
        logger.lifecycle("DSGVODataStore files cleared! ${fileExcelBuffer.absolutePath}")
    }
}

afterEvaluate {
    tasks.named("compileDebugKotlin") {
        dependsOn("clearDSGVODataStoreFiles")
    }
}