package de.klyk.annotationprocessorexcel.processor

/**
 * Data class to store the DSGVO information.
 */
data class DsgvoInfoData(
    var kategorie: List<String> = emptyList(),
    var verwendungsZweck: List<String> = emptyList(),
    var land: String = ""
)