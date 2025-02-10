package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.Solution
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDsgvoExport
import de.klyk.annotationprocessorexcel.processor.annotations.DatenKategorie
import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
import de.klyk.annotationprocessorexcel.processor.annotations.SystemCluster
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
import de.klyk.annotationprocessorexcel.processor.annotations.kategorieEmpfaenger

@DsgvoClass(
    datenKategorie = [DatenKategorie.MITARBEITER, DatenKategorie.BESTANDSKUNDE],
    verwendungszweck = [Verwendungszweck.KUNDENBINDUNG, Verwendungszweck.LOGGING],
    solution = Solution.FINANZEN,
    system = SystemCluster.FRONTEND,
    personenbezogeneDaten = PersonenbezogeneDaten.JA,
    datenquellen = "risus",
    kategorieEmpfaenger = [kategorieEmpfaenger.MITARBEITER],
    datenVerschluesselt = false,
    beteiligteLaender = "DE; FR",
    bemerkungen = "leo",
    optionaleTechnischeInformationen = "iaculis",

)
data class Employee(
    val name: String,
    val concern: String,
    val phoneNumber: String,
    @ExcludeFromDsgvoExport
    val email: String,
)