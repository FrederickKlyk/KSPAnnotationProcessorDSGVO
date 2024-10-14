package de.klyk.annotationprocessorexcel.processor.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DsgvoExportExcel(
    val kategorie: Kategorie,
    val verwendungszweck: Verwendungszweck,
    val laender: String
)

enum class Kategorie {
    BESTANDSKUNDE,
    MITARBEITER
}

enum class Verwendungszweck {
    KUNDENWERBUNG,
    KUNDENBINDUNG
}

