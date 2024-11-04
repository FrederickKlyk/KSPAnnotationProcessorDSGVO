package de.klyk.annotationprocessorexcel.processor.visitor.helper

import com.google.devtools.ksp.symbol.KSValueArgument
import kotlin.reflect.KClass

/**
 * Helper Methods: Extract the strings from the annotation arguments
 */
internal object VisitorExtractHelper {
    fun List<KSValueArgument>.extractStringsFromAnnotationArgumentString(dsgvoAnnotationArgument: String) =
        find { it.name?.asString()?.lowercase() == dsgvoAnnotationArgument.lowercase() }?.value as? String

    fun List<KSValueArgument>.extractStringsFromAnnotationArgumentBoolean(dsgvoAnnotationArgument: String) =
        find { it.name?.asString()?.lowercase() == dsgvoAnnotationArgument.lowercase() }?.value as? Boolean

    // Extract the string from the annotation argument
    fun List<KSValueArgument>.extractStringsFromAnnotationArgumentEnum(dsgvoAnnotationArgument: String) =
        (find { it.name?.asString()?.lowercase() == dsgvoAnnotationArgument.lowercase() }?.value.toString()).substringAfterLast(".")

    // Extract the strings from the annotation argument Array
    fun List<KSValueArgument>.extractStringsFromAnnotationArgumentEnumArray(dsgvoAnnotationArgument: String) =
        (find { it.name?.asString()?.lowercase() == dsgvoAnnotationArgument.lowercase() }?.value as? List<*>)?.map {
            it.toString().substringAfterLast('.')
        } ?: emptyList()

    fun KClass<*>.toSimpleNameString() = simpleName ?: ""

    fun List<String>.separatorKommaForExport(): String {
        return this.joinToString(", ").trim { it <= ' ' || it == ',' }
    }
}