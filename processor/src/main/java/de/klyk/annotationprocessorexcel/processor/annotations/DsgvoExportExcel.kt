package de.klyk.annotationprocessorexcel.processor.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DsgvoExportExcel(
    val kategorie: Kategorie,
    val verwendungszweck: String,
    val land: String
)

enum class Kategorie {
    BESTANDSKUNDE,
    MITARBEITER
}