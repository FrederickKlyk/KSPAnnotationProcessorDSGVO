package de.klyk.annotationprocessorexcel.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import de.klyk.annotationprocessorexcel.processor.DsgvoInfoData
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoProperty
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoPropertyData

class DsgvoExportVisitor(val logger: KSPLogger) : KSVisitorVoid() {
    private val csvData = StringBuilder()
    private val excelData = mutableListOf<ExcelRow>()
    private val excludedProperties = mutableSetOf<String>()

    // Visit all classes and delegate first properties
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        // First process all properties of the class
        classDeclaration.getAllProperties().forEach { propertyDeclaration ->
            propertyDeclaration.accept(this, Unit)
        }

        // Then process the class for export
        classDeclaration.annotations.find { it.shortName.asString() == AnnotationConstants.ANNOTATION_DSGVO_CLASS_NAME }?.let {
            classDeclaration.processDsgvoDataExport()
        }
    }

    // Visit all properties
    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        property.getExcludedPropertiesFromExcludeFromDsgvoAnnotation()
    }

    fun getCsvData(): String = csvData.toString()

    fun getExcelData(): List<ExcelRow> = excelData

    private fun KSPropertyDeclaration.getExcludedPropertiesFromExcludeFromDsgvoAnnotation() {
        annotations.find { it.shortName.asString() == AnnotationConstants.ANNOTATION_EXCLUDE_FROM_DSGVO_NAME }?.let {
            excludedProperties.add(simpleName.asString())
        }
    }

    private fun KSPropertyDeclaration.getDsgvoPropertyDataFromDsgvoPropertyAnnotation() =
        annotations.find { it.shortName.asString() == AnnotationConstants.ANNOTATION_DSGVO_PROPERTY_NAME }?.let {
            getDsgvoPropertyData()
        }

    private fun KSClassDeclaration.processDsgvoDataExport() {
        val dsgvoInfoData = getDsgvoInfoData()
        val className = simpleName.asString()

        val dsgvoPropertiesFromAnnotation = getAllProperties().mapNotNull { property ->
            if (property.simpleName.asString() !in excludedProperties) {
                property.getDsgvoPropertyDataFromDsgvoPropertyAnnotation()
            } else {
                null
            }
        }

        val combinedPersonenbezogeneDaten = dsgvoInfoData.personenbezogeneDaten + " (" + getAllProperties()
            .filter { it.simpleName.asString() !in excludedProperties }
            .joinToString(", ") { it.simpleName.asString() } + ")"

        dsgvoInfoData.verwendungszweck.forEach { verwendungsZweck ->
            csvData.append(className).append(", ")
                .append("(${dsgvoInfoData.kategorie.joinToString(", ")})").append(", ")
                .append(verwendungsZweck).append(", ")
                .append(dsgvoInfoData.land).append(", ")
                .append(dsgvoInfoData.domaene).append(", ")
                .append(dsgvoInfoData.system).append(", ")
                .append(combinedPersonenbezogeneDaten).append(", ")
                .append(dsgvoInfoData.quellen).append(", ")
                .append("(${dsgvoInfoData.kategorieVonEmpfaengern.joinToString(", ")})").append(", ")
                .append(dsgvoInfoData.drittland).append(", ")
                .append(dsgvoInfoData.bemerkungen).append(", ")
                .append(dsgvoInfoData.optionaleTechnischeInformationen).append("\n")
        }

        dsgvoPropertiesFromAnnotation.forEach { property ->
            property.verwendungszweck.forEach { verwendungsZweck ->
                csvData.append(className).append(", ")
                    .append("(${dsgvoInfoData.kategorie.joinToString(", ")})").append(", ")
                    .append("$verwendungsZweck (${property.name})").append(", ")
                    .append(dsgvoInfoData.land).append(", ")
                    .append(dsgvoInfoData.domaene).append(", ")
                    .append(dsgvoInfoData.system).append(", ")
                    .append(combinedPersonenbezogeneDaten).append(", ")
                    .append(dsgvoInfoData.quellen).append(", ")
                    .append("(${dsgvoInfoData.kategorieVonEmpfaengern.joinToString(", ")})").append(", ")
                    .append(dsgvoInfoData.drittland).append(", ")
                    .append(dsgvoInfoData.bemerkungen).append(", ")
                    .append(dsgvoInfoData.optionaleTechnischeInformationen).append("\n")
            }
        }

        excelData.add(ExcelRow(className, dsgvoInfoData.copy(personenbezogeneDaten = combinedPersonenbezogeneDaten), dsgvoPropertiesFromAnnotation))
    }

    private fun KSPropertyDeclaration.getDsgvoPropertyData(): DsgvoPropertyData? {
        val annotation = annotations.find { it.shortName.asString() == DsgvoProperty::class.simpleName }
        return annotation?.let {
            DsgvoPropertyData(
                name = simpleName.asString(),
                verwendungszweck = (it.arguments.find { arg -> arg.name?.asString() == AnnotationConstants.VERWENDUNGSZWECK_PROPERTY }?.value as? List<*>)?.map { v ->
                    v.toString().substringAfterLast(".")
                } ?: emptyList()
            )
        }
    }

    private fun KSClassDeclaration.getDsgvoInfoData(): DsgvoInfoData {
        val annotation = annotations.find { it.shortName.asString() == AnnotationConstants.ANNOTATION_DSGVO_CLASS_NAME }

        return annotation?.arguments?.let { args ->
            DsgvoInfoData(
                kategorie = (args.find { it.name?.asString() == AnnotationConstants.KATEGORIE.lowercase() }?.value as? List<*>)?.map {
                    it.toString().substringAfterLast('.')
                } ?: emptyList(),
                verwendungszweck = (args.find { it.name?.asString() == AnnotationConstants.VERWENDUNGSZWECK.lowercase() }?.value as? List<*>)?.map {
                    it.toString().substringAfterLast('.')
                } ?: emptyList(),
                land = args.find { it.name?.asString() == AnnotationConstants.LAND.lowercase() }?.value as? String ?: "Deutschland",
                domaene = (args.find { it.name?.asString() == AnnotationConstants.DOMAENE.lowercase() }?.value.toString()).substringAfterLast("."),
                system = args.find { it.name?.asString() == AnnotationConstants.SYSTEM.lowercase() }?.value.toString().substringAfterLast("."),
                personenbezogeneDaten = args.find { it.name?.asString() == AnnotationConstants.PERSONENBEZOGENE_DATEN }?.value.toString()
                    .substringAfterLast("."),
                quellen = args.find { it.name?.asString() == AnnotationConstants.QUELLEN.lowercase() }?.value as? String ?: "",
                kategorieVonEmpfaengern = (args.find { it.name?.asString() == AnnotationConstants.KATEGORIE_VON_EMPFAENGERN }?.value as? List<*>)?.map {
                    it.toString().substringAfterLast('.')
                } ?: emptyList(),
                drittland = args.find { it.name?.asString() == AnnotationConstants.DRITTLAND.lowercase() }?.value as? Boolean ?: false,
                bemerkungen = args.find { it.name?.asString() == AnnotationConstants.BEMERKUNGEN.lowercase() }?.value as? String ?: "",
                optionaleTechnischeInformationen = args.find { it.name?.asString() == AnnotationConstants.OPTIONALE_TECHNISCHE_INFORMATIONEN }?.value as? String
                    ?: ""
            )
        } ?: DsgvoInfoData()
    }
}

data class ExcelRow(
    val className: String,
    val dsgvoInfoData: DsgvoInfoData,
    val properties: Sequence<DsgvoPropertyData>
)