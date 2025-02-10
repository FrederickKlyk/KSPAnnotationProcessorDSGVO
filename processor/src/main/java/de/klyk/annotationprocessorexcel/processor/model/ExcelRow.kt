package de.klyk.annotationprocessorexcel.processor.model

import de.klyk.annotationprocessorexcel.processor.annotations.DSGVOPropertyRelevantData

import kotlinx.serialization.Serializable

@Serializable
data class ExcelRow(
    val className: String,
    val DSGVORelevantData: DSGVORelevantDataDto,
    val DSGVOPropertyRelevantData: List<DSGVOPropertyRelevantData>
)