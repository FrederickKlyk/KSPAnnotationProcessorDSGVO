package de.klyk.annotationprocessorexcel.processor.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DsgvoExportExcel(
    val sheetName: String,
    val kategorie: String,
    val verwendungszweck: String,
    val land: String
)