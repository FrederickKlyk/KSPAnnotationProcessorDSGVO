package de.klyk.annotationprocessorexcel

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel

@DsgvoExportExcel(
    sheetName = "frontend zu backend",
    kategorie = "Bestandskunde",
    verwendungszweck = "Kundenwerbung",
    land = "Deutschland"
)
data class Person(
    val name: String,
    val age: Int,
    val email: String
)
