package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel

@DsgvoExportExcel(
    kategorie = "Mitarbeiter",
    verwendungszweck = "Mitarbeiterverwaltung",
    land = "Deutschland"
)
data class Employee(
    val name: String,
    val concern: String,
    val phoneNumber: String
)