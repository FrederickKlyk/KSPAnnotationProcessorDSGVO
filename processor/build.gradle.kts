plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.seriazliation)
}

dependencies {
    // KSP
    implementation(libs.symbol.processing.api)

    // Excel Export
    implementation(libs.poi.ooxml)
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()
}
