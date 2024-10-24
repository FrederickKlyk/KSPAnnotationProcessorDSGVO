package de.klyk.annotationprocessorexcel.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import de.klyk.annotationprocessorexcel.processor.DsgvoInfoData
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoProperty
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoPropertyData
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentBoolean
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentEnum
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentEnumArray
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentEnumArrayWithoutLowercase
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentEnumWithoutLowercase
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentString

internal class DsgvoExportVisitor(val logger: KSPLogger) : KSVisitorVoid() {
    private val csvData = StringBuilder()
    private val excelData = mutableListOf<ExcelRow>()
    private val excludedProperties = mutableSetOf<String>()

    // Visit all classes and delegate first properties
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        logger.warn("Processing class in visitClassDeclaration: ${classDeclaration.simpleName.asString()}")
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
        logger.warn("Processing property in visitPropertyDeclaration: ${property.simpleName.asString()}")
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

    private fun KSPropertyDeclaration.getDsgvoPropertyData(): DsgvoPropertyData? {
        val annotation = annotations.find { it.shortName.asString() == DsgvoProperty::class.simpleName }
        return annotation?.let {
            DsgvoPropertyData(
                name = simpleName.asString(),
                verwendungszweck = it.arguments.extractStringsFromAnnotationArgumentEnumArrayWithoutLowercase(AnnotationConstants.VERWENDUNGSZWECK_PROPERTY)
            )
        }
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

        dsgvoInfoData.personenbezogeneDaten = dsgvoInfoData.personenbezogeneDaten + " (" +
                getAllProperties()
                    .filter { it.simpleName.asString() !in excludedProperties }
                    .joinToString(". ") { it.simpleName.asString() } + ")"

        dsgvoInfoData.verwendungszweck.forEach { verwendungsZweck ->
            csvData.append(className).append(", ")
                .append("(${dsgvoInfoData.kategorie.joinToString(". ")})").append(", ")
                .append(verwendungsZweck).append(", ")
                .append(dsgvoInfoData.land).append(", ")
                .append(dsgvoInfoData.domaene).append(", ")
                .append(dsgvoInfoData.system).append(", ")
                .append(dsgvoInfoData.personenbezogeneDaten).append(", ")
                .append(dsgvoInfoData.quellen).append(", ")
                .append("(${dsgvoInfoData.kategorieVonEmpfaengern.joinToString(". ")})").append(", ")
                .append(dsgvoInfoData.drittland).append(", ")
                .append(dsgvoInfoData.bemerkungen).append(", ")
                .append(dsgvoInfoData.optionaleTechnischeInformationen).append("\n")
        }

        dsgvoPropertiesFromAnnotation.forEach { property ->
            property.verwendungszweck.forEach { verwendungsZweck ->
                csvData.append(className).append(", ")
                    .append("(${dsgvoInfoData.kategorie.joinToString(". ")})").append(", ")
                    .append("$verwendungsZweck (${property.name})").append(", ")
                    .append(dsgvoInfoData.land).append(", ")
                    .append(dsgvoInfoData.domaene).append(", ")
                    .append(dsgvoInfoData.system).append(", ")
                    .append(dsgvoInfoData.personenbezogeneDaten).append(", ")
                    .append(dsgvoInfoData.quellen).append(", ")
                    .append("(${dsgvoInfoData.kategorieVonEmpfaengern.joinToString(". ")})").append(", ")
                    .append(dsgvoInfoData.drittland).append(", ")
                    .append(dsgvoInfoData.bemerkungen).append(", ")
                    .append(dsgvoInfoData.optionaleTechnischeInformationen).append("\n")
            }
        }

        excelData.add(ExcelRow(className, dsgvoInfoData, dsgvoPropertiesFromAnnotation))
    }

    private fun KSClassDeclaration.getDsgvoInfoData(): DsgvoInfoData {
        val annotation = annotations.find { it.shortName.asString() == AnnotationConstants.ANNOTATION_DSGVO_CLASS_NAME }

        return annotation?.arguments?.let { args ->
            DsgvoInfoData(
                kategorie = args.extractStringsFromAnnotationArgumentEnumArray(AnnotationConstants.KATEGORIE),
                verwendungszweck = args.extractStringsFromAnnotationArgumentEnumArray(AnnotationConstants.VERWENDUNGSZWECK),
                land = args.extractStringsFromAnnotationArgumentString(AnnotationConstants.LAND) ?: "Deutschland",
                domaene = args.extractStringsFromAnnotationArgumentEnum(AnnotationConstants.DOMAENE),
                system = args.extractStringsFromAnnotationArgumentEnum(AnnotationConstants.SYSTEM),
                personenbezogeneDaten = args.extractStringsFromAnnotationArgumentEnumWithoutLowercase(AnnotationConstants.PERSONENBEZOGENE_DATEN),
                quellen = args.extractStringsFromAnnotationArgumentString(AnnotationConstants.QUELLEN) ?: "",
                kategorieVonEmpfaengern = args.extractStringsFromAnnotationArgumentEnumArrayWithoutLowercase(AnnotationConstants.KATEGORIE_VON_EMPFAENGERN),
                drittland = args.extractStringsFromAnnotationArgumentBoolean(AnnotationConstants.DRITTLAND) ?: false,
                bemerkungen = args.extractStringsFromAnnotationArgumentString(AnnotationConstants.BEMERKUNGEN) ?: "",
                optionaleTechnischeInformationen = args.extractStringsFromAnnotationArgumentString(AnnotationConstants.OPTIONALE_TECHNISCHE_INFORMATIONEN)
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