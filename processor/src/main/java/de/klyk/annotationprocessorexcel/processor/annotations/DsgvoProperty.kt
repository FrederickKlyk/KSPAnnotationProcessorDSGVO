package de.klyk.annotationprocessorexcel.processor.annotations

import kotlinx.serialization.Serializable

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DsgvoProperty(
    val verwendungszweckProperty: Array<Verwendungszweck>
)

@Serializable
data class DsgvoPropertyRelevantData(
    val name: String,
    val verwendungszweck: List<String>
)