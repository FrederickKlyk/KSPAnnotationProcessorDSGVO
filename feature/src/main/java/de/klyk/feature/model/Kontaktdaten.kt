package de.klyk.feature.model

import de.klyk.annotationprocessorexcel.processor.annotations.Solution
import de.klyk.annotationprocessorexcel.processor.annotations.DSGVOClass
import de.klyk.annotationprocessorexcel.processor.annotations.DSGVOProperty
import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDSGVOExport
import de.klyk.annotationprocessorexcel.processor.annotations.Datenkategorie
import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
import de.klyk.annotationprocessorexcel.processor.annotations.SystemCluster
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
import de.klyk.annotationprocessorexcel.processor.annotations.kategorieEmpfaenger

@DSGVOClass(
    solution = Solution.CRM,
    system = SystemCluster.FRONTEND,
    datenkategorie = [Datenkategorie.BESTANDSKUNDE],
    personenbezogeneDaten = PersonenbezogeneDaten.JA,
    verwendungszweck = [Verwendungszweck.KUNDENVERWALTUNG],
    datenquellen = "CRM; Kunde",
    kategorieEmpfaenger = [kategorieEmpfaenger.MITARBEITER],
    datenVerschluesselt = false,
    beteiligteLaender = "DE; FR; NL; IT",
    bemerkungen = "Perspektivisch Marketing möglich.",
    optionaleTechnischeInformationen = "Keine",
)
data class Kontaktdaten(
    val name: String,
    val wohnort: String,
    val alter: Int,
    val telefonnummer: Number,
    @DSGVOProperty([Verwendungszweck.KUNDENBINDUNG])
    val email: String,
    @ExcludeFromDSGVOExport
    val irrelevantInfo: String,
    @ExcludeFromDSGVOExport
    val irrelevantInfo2: String,
    val relevanteInfo: String
)



fun test(){
    Kontaktdaten()
}