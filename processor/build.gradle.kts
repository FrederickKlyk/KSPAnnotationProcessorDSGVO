plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.seriazliation)
}

sourceSets {
    test {
        java.srcDir("src/test/java")
    }
}

dependencies {
    //KSP
    implementation(libs.symbol.processing.api)
    implementation(libs.poi.ooxml)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.compile.testing.ksp)
}