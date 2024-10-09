package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel
import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie

@DsgvoExportExcel(
    kategorie = Kategorie.BESTANDSKUNDE,
    verwendungszweck = "Kundenwerbung",
    land = "Deutschland"
)
data class Person(
    val name: String,
    val age: Int,
    val email: String
)

