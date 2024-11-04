plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.seriazliation)
}

dependencies {
    //KSP
    implementation(libs.symbol.processing.api)
    implementation(libs.poi.ooxml)
    implementation(libs.kotlinx.serialization.json)
}