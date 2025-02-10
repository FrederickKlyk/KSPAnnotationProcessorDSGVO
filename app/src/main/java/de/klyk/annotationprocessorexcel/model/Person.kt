package de.klyk.annotationprocessorexcel.model

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
    datenkategorie = [Datenkategorie.BESTANDSKUNDE],
    verwendungszweck = [Verwendungszweck.LOGGING, Verwendungszweck.RECOVERY],
    beteiligteLaender = "DE; FR; NL; IT",
    solution = Solution.CRM,
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
    @DSGVOProperty([Verwendungszweck.KUNDENWERBUNG, Verwendungszweck.KUNDENBINDUNG])
    val email: String,
    @ExcludeFromDSGVOExport
    val irrelevantInfo: String,
    val relevanteInfo: String
)

