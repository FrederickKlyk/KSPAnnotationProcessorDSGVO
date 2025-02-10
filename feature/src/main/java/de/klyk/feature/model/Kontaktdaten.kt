package de.klyk.feature.model

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
    verwendungszweck = [Verwendungszweck.LOGGING],
    beteiligteLaender = "DE; FR; NL; IT",
    solution = Solution.CRM,
    system = SystemCluster.FRONTEND,
    personenbezogeneDaten = PersonenbezogeneDaten.JA,
    datenquellen = "Kunde",
    kategorieEmpfaenger = [kategorieEmpfaenger.KUNDEN],
    datenVerschluesselt = false,
    bemerkungen = "Erstmal keine",
    optionaleTechnischeInformationen = "Auch keine11",
)
data class Kontaktdaten(
    val name: String,
    val wohnort: String,
    val age: Int,
    val phoneNumber: Number,
    @DsgvoProperty([Verwendungszweck.KUNDENBINDUNG])
    val email: String,
    @ExcludeFromDsgvoExport
    val irrelevantInfo: String,
    @ExcludeFromDsgvoExport
    val irrelevantInfo2: String,
    val relevanteInfo: String
)