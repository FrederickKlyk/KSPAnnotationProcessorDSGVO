package de.klyk.annotationprocessorexcel.processor.visitor

import com.google.devtools.ksp.symbol.*
import de.klyk.annotationprocessorexcel.processor.DsgvoInfoData
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.ANNOTATION_DSGVO_NAME
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.ANNOTATION_EXCLUDE_FROM_DSGVO_NAME

class DsgvoExportVisitor : KSVisitorVoid() {
    private val csvData = StringBuilder()
    private val excelData = mutableListOf<ExcelRow>()
    private val excludedProperties = mutableSetOf<String>()

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        // Iterate over all properties and visit them first
        classDeclaration.getAllProperties().forEach { it.accept(this, Unit) }

        // Then process the class for export
        classDeclaration.annotations.find { it.shortName.asString() == ANNOTATION_DSGVO_NAME }?.let {
            processDsgvoExport(classDeclaration, it)
        }
    }

    private fun processDsgvoExport(classDeclaration: KSClassDeclaration, annotation: KSAnnotation) {
        val dsgvoInfoData = classDeclaration.getDsgvoInfoData()
        val className = classDeclaration.simpleName.asString()

        csvData.append("${AnnotationConstants.DATENKLASSE_NAME}, ${AnnotationConstants.DATENKLASSE_PROPERTY}, ${AnnotationConstants.KATEGORIE},${AnnotationConstants.VERWENDUNGSZWECK},${AnnotationConstants.LAND}\n")
        val properties = classDeclaration.getAllProperties().mapNotNull { property ->
            if (property.simpleName.asString() !in excludedProperties) {
                csvData.append("$className,${property.simpleName.asString()},${dsgvoInfoData.kategorie},${dsgvoInfoData.verwendungsZweck},${dsgvoInfoData.land}\n")
                property.simpleName.asString()
            } else {
                null
            }
        }.toList()

        excelData.add(ExcelRow(className, dsgvoInfoData, properties))
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        property.annotations.find { it.shortName.asString() == ANNOTATION_EXCLUDE_FROM_DSGVO_NAME }?.let {
            processExcludeFromDsgvoExport(property, it)
        }
    }

    private fun processExcludeFromDsgvoExport(property: KSPropertyDeclaration, annotation: KSAnnotation) {
        excludedProperties.add(property.simpleName.asString())
    }

    fun getCsvData(): String = csvData.toString()

    fun getExcelData(): List<ExcelRow> = excelData

    private fun KSClassDeclaration.getDsgvoInfoData(): DsgvoInfoData {
        val annotation = annotations.find { it.shortName.asString() == AnnotationConstants.ANNOTATION_DSGVO_NAME }

        return annotation?.arguments?.let { args ->
            DsgvoInfoData(
                kategorie = (args.find { it.name?.asString() == AnnotationConstants.KATEGORIE.lowercase() }?.value as? List<*>)?.map {
                    it.toString().substringAfterLast('.')
                } ?: emptyList(),
                verwendungsZweck = (args.find { it.name?.asString() == AnnotationConstants.VERWENDUNGSZWECK.lowercase() }?.value as? List<*>)?.map {
                    it.toString().substringAfterLast('.')
                } ?: emptyList(),
                land = args.find { it.name?.asString() == AnnotationConstants.LAND.lowercase() }?.value as? String ?: "Deutschland"
            )
        } ?: DsgvoInfoData()
    }
}

data class ExcelRow(
    val className: String,
    val dsgvoInfoData: DsgvoInfoData,
    val properties: List<String>
)