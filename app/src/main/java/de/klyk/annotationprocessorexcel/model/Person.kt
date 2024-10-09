package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel

@DsgvoExportExcel(
    kategorie = "Bestandskunde",
    verwendungszweck = "Kundenwerbung",
    land = "Deutschland"
)
data class Person(
    val name: String,
    val age: Int,
    val email: String
)
