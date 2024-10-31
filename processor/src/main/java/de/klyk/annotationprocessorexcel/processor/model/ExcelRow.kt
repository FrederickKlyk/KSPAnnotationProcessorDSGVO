package de.klyk.annotationprocessorexcel.processor.model

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoPropertyRelevantData

data class ExcelRow(
    val className: String,
    val dsgvoRelevantData: DsgvoRelevantDataDto,
    val dsgvoPropertyRelevantData: Sequence<DsgvoPropertyRelevantData>
)