package de.klyk.annotationprocessorexcel.processor.model

import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoPropertyRelevantData

import kotlinx.serialization.Serializable

@Serializable
data class ExcelRow(
    val className: String,
    val dsgvoRelevantData: DsgvoRelevantDataDto,
    val dsgvoPropertyRelevantData: List<DsgvoPropertyRelevantData>
)