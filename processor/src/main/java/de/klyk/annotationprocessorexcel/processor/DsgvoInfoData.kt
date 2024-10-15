package de.klyk.annotationprocessorexcel.processor

data class DsgvoInfoData(
    var kategorie: List<String> = emptyList(),
    var verwendungsZweck: List<String> = emptyList(),
    var land: String = ""
)