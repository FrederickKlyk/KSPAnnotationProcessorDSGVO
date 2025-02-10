package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.Solution
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoProperty
import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDsgvoExport
import de.klyk.annotationprocessorexcel.processor.annotations.DatenKategorie
import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
import de.klyk.annotationprocessorexcel.processor.annotations.SystemCluster
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
import de.klyk.annotationprocessorexcel.processor.annotations.kategorieEmpfaenger

@DsgvoClass(
    datenKategorie = [DatenKategorie.BESTANDSKUNDE],
    verwendungszweck = [Verwendungszweck.LOGGING, Verwendungszweck.RECOVERY],
    beteiligteLaender = "DE; FR; NL; IT",
    solution = Solution.FINANZEN,
    system = SystemCluster.FRONTEND,
    personenbezogeneDaten = PersonenbezogeneDaten.JA,
    datenquellen = "partiendo",
    kategorieEmpfaenger = [kategorieEmpfaenger.KUNDEN],
    datenVerschluesselt = false,
    bemerkungen = "laoreet",
    optionaleTechnischeInformationen = "keine",
)
data class Person(
    val name: String,
    val age: Int,
    val phoneNumber: Number,
    @DsgvoProperty([Verwendungszweck.KUNDENWERBUNG, Verwendungszweck.KUNDENBINDUNG])
    val email: String,
    @ExcludeFromDsgvoExport
    val irrelevantInfo: String,
    val relevanteInfo: String
)

