package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel
import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDsgvoExport
import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck

@DsgvoExportExcel(
    kategorie = [Kategorie.BESTANDSKUNDE],
    verwendungszweck = [Verwendungszweck.KUNDENBINDUNG, Verwendungszweck.KUNDENWERBUNG],
    land = "DE, FR, AT"
)
data class Person(
    val name: String,
    val age: Int,
    val email: String,
    @ExcludeFromDsgvoExport
    val irrelevantInfo: String,
    val relevanteInfo: String
)

