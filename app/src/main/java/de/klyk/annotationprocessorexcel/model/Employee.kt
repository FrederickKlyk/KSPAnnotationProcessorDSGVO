package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel
import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie

@DsgvoExportExcel(
    kategorie = Kategorie.MITARBEITER,
    verwendungszweck = "Mitarbeiterverwaltung",
    land = "Deutschland"
)
data class Employee(
    val name: String,
    val concern: String,
    val phoneNumber: String
)