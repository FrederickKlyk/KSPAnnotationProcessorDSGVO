package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.Solution
import de.klyk.annotationprocessorexcel.processor.annotations.DSGVOClass
import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDSGVOExport
import de.klyk.annotationprocessorexcel.processor.annotations.Datenkategorie
import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
import de.klyk.annotationprocessorexcel.processor.annotations.SystemCluster
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
import de.klyk.annotationprocessorexcel.processor.annotations.kategorieEmpfaenger

@DSGVOClass(
    datenkategorie = [Datenkategorie.MITARBEITER],
    verwendungszweck = [Verwendungszweck.RECOVERY, Verwendungszweck.LOGGING],
    solution = Solution.HR,
    system = SystemCluster.FRONTEND,
    personenbezogeneDaten = PersonenbezogeneDaten.JA,
    datenquellen = "HR",
    kategorieEmpfaenger = [kategorieEmpfaenger.MITARBEITER, kategorieEmpfaenger.DIENSTLEISTER],
    datenVerschluesselt = false,
    beteiligteLaender = "DE, AT",
    bemerkungen = "Ab 2026 weitere Länder",
    optionaleTechnischeInformationen = "Verschlüsselung ab 2026",

)
data class Employee(
    val name: String,
    val concern: String,
    val phoneNumber: String,
    @ExcludeFromDSGVOExport
    val email: String,
)