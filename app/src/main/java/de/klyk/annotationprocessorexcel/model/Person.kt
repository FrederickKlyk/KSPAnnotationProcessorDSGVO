package de.klyk.annotationprocessorexcel.model

import de.klyk.annotationprocessorexcel.processor.annotations.Domaene
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoProperty
import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDsgvoExport
import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie
import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
import de.klyk.annotationprocessorexcel.processor.annotations.SystemCluster
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
import de.klyk.annotationprocessorexcel.processor.annotations.kategorieVonEmpfaengern

@DsgvoClass(
    kategorie = [Kategorie.BESTANDSKUNDE],
    verwendungszweck = [Verwendungszweck.LOGGING, Verwendungszweck.RECOVERY],
    land = "DE, FR, AT",
    domaene = Domaene.FINANZEN,
    system = SystemCluster.FRONTEND,
    personenbezogeneDaten = PersonenbezogeneDaten.JA,
    quellen = "partiendo",
    kategorieVonEmpfaengern = [kategorieVonEmpfaengern.KUNDEN],
    drittland = false,
    bemerkungen = "laoreet",
    optionaleTechnischeInformationen = "curae",
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

