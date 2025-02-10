package de.klyk.annotationprocessorexcel.processor.annotations

import kotlinx.serialization.Serializable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DSGVOProperty(
    val verwendungszweckProperty: Array<Verwendungszweck>
)

@Serializable
data class DSGVOPropertyRelevantData(
    val name: String,
    val verwendungszweck: List<String>,
    val displayName: String = ""
)