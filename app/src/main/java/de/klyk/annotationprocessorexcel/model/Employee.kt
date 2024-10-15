package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel
import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck

@DsgvoExportExcel(
    kategorie = [Kategorie.MITARBEITER],
    verwendungszweck = [Verwendungszweck.KUNDENBINDUNG],
    laender = "Deutschland"
)
data class Employee(
    val name: String,
    val concern: String,
    val phoneNumber: String
)