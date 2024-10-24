package de.klyk.annotationprocessorexcel.processor.visitor.helper

import com.google.devtools.ksp.symbol.KSValueArgument

/**
 * Helper Methods: Extract the strings from the annotation arguments
 */
internal object VisitorExtractHelper {
     fun List<KSValueArgument>.extractStringsFromAnnotationArgumentString(dsgvoAnnotationArgument: String) =
        find { it.name?.asString() == dsgvoAnnotationArgument.lowercase() }?.value as? String

     fun List<KSValueArgument>.extractStringsFromAnnotationArgumentBoolean(dsgvoAnnotationArgument: String) =
        find { it.name?.asString() == dsgvoAnnotationArgument.lowercase() }?.value as? Boolean

    // Extract the string from the annotation argument
     fun List<KSValueArgument>.extractStringsFromAnnotationArgumentEnum(dsgvoAnnotationArgument: String) =
        (find { it.name?.asString() == dsgvoAnnotationArgument.lowercase() }?.value.toString()).substringAfterLast(".")

     fun List<KSValueArgument>.extractStringsFromAnnotationArgumentEnumWithoutLowercase(dsgvoAnnotationArgument: String) =
        (find { it.name?.asString() == dsgvoAnnotationArgument }?.value.toString()).substringAfterLast(".")

    // Extract the strings from the annotation argument Array
     fun List<KSValueArgument>.extractStringsFromAnnotationArgumentEnumArray(dsgvoAnnotationArgument: String) =
        (find { it.name?.asString() == dsgvoAnnotationArgument.lowercase() }?.value as? List<*>)?.map {
            it.toString().substringAfterLast('.')
        } ?: emptyList()

    fun List<KSValueArgument>.extractStringsFromAnnotationArgumentEnumArrayWithoutLowercase(dsgvoAnnotationArgument: String) =
        (find { it.name?.asString() == dsgvoAnnotationArgument }?.value as? List<*>)?.map {
            it.toString().substringAfterLast('.')
        } ?: emptyList()
}