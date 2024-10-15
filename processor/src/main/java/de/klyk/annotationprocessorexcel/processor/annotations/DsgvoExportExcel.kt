package de.klyk.annotationprocessorexcel.processor.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DsgvoExportExcel(
    val kategorie: Array<Kategorie>,
    val verwendungszweck: Array<Verwendungszweck>,
    val land: String
)

enum class Kategorie {
    BESTANDSKUNDE,
    MITARBEITER
}

enum class Verwendungszweck {
    KUNDENWERBUNG,
    KUNDENBINDUNG
}

