package de.klyk.annotationprocessorexcel.processor.model

import kotlinx.serialization.Serializable

/**
 * Data class to store the DSGVO information.
 */
@Serializable
data class DSGVORelevantDataDto(
    var datenkategorie: List<String> = emptyList(),
    var verwendungszweck: List<String> = emptyList(),
    var beteiligteLaender: String = "",
    var solution: String = "",
    var system: String = "",
    var personenbezogeneDaten: String = "",
    var datenquellen: String = "",
    var kategorieEmpfaenger: List<String> = emptyList(),
    var datenVerschluesselt: Boolean = false,
    var bemerkungen: String = "",
    var optionaleTechnischeInformationen: String = ""
)