package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.Domaene
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie
import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
import de.klyk.annotationprocessorexcel.processor.annotations.SystemCluster
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
import de.klyk.annotationprocessorexcel.processor.annotations.kategorieVonEmpfaengern

@DsgvoClass(
    kategorie = [Kategorie.MITARBEITER, Kategorie.BESTANDSKUNDE],
    verwendungszweck = [Verwendungszweck.KUNDENBINDUNG],
    domaene = Domaene.FINANZEN,
    system = SystemCluster.FRONTEND,
    personenbezogeneDaten = PersonenbezogeneDaten.JA,
    quellen = "risus",
    kategorieVonEmpfaengern = [kategorieVonEmpfaengern.MITARBEITER],
    drittland = false,
    land = "DE, FR",
    bemerkungen = "leo",
    optionaleTechnischeInformationen = "iaculis",

)
data class Employee(
    val name: String,
    val concern: String,
    val phoneNumber: String,
    val email: String,
)