package de.klyk.annotationprocessorexcel.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import de.klyk.annotationprocessorexcel.processor.annotations.Domaene
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoProperty
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoPropertyRelevantData
import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDsgvoExport
import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie
import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
import de.klyk.annotationprocessorexcel.processor.annotations.kategorieVonEmpfaengern
import de.klyk.annotationprocessorexcel.processor.model.DsgvoRelevantDataDto
import de.klyk.annotationprocessorexcel.processor.model.ExcelRow
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentBoolean
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentEnum
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentEnumArray
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentString
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.toSimpleNameString

/**
 * the visitor is only responsible for data collection, while the processor handles the creation and extraction of the Excel data,
 * adhering to the separation of concerns principle.
 */
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
        classDeclaration.annotations.find { it.shortName.asString() == DsgvoClass::class.simpleName }?.let {
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
        annotations.find { it.shortName.asString() == ExcludeFromDsgvoExport::class.simpleName }?.let {
            excludedProperties.add(simpleName.asString())
        }
    }

    private fun KSPropertyDeclaration.getDsgvoPropertyDataFromDsgvoPropertyAnnotation(): DsgvoPropertyRelevantData? {
        val annotation = annotations.find { it.shortName.asString() == DsgvoProperty::class.simpleName }
        return annotation?.let {
            DsgvoPropertyRelevantData(
                name = simpleName.asString(),
                verwendungszweck = it.arguments.extractStringsFromAnnotationArgumentEnumArray(DsgvoProperty::verwendungszweckProperty.name)
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
                    .append(verwendungsZweck).append(", ")
                    .append(dsgvoInfoData.land).append(", ")
                    .append(dsgvoInfoData.domaene).append(", ")
                    .append(dsgvoInfoData.system).append(", ")
                    .append(property.name).append(", ")
                    .append(dsgvoInfoData.quellen).append(", ")
                    .append("(${dsgvoInfoData.kategorieVonEmpfaengern.joinToString(". ")})").append(", ")
                    .append(dsgvoInfoData.drittland).append(", ")
                    .append(dsgvoInfoData.bemerkungen).append(", ")
                    .append(dsgvoInfoData.optionaleTechnischeInformationen).append("\n")
            }
        }

        excelData.add(ExcelRow(className, dsgvoInfoData, dsgvoPropertiesFromAnnotation))
    }

    private fun KSClassDeclaration.getDsgvoInfoData(): DsgvoRelevantDataDto {
        val annotation = annotations.find { it.shortName.asString() == DsgvoClass::class.simpleName }

        return annotation?.arguments?.let { args ->
            DsgvoRelevantDataDto(
                kategorie = args.extractStringsFromAnnotationArgumentEnumArray(Kategorie::class.toSimpleNameString()),
                verwendungszweck = args.extractStringsFromAnnotationArgumentEnumArray(Verwendungszweck::class.toSimpleNameString()),
                land = args.extractStringsFromAnnotationArgumentString(DsgvoClass::land.name) ?: "",
                domaene = args.extractStringsFromAnnotationArgumentEnum(Domaene::class.toSimpleNameString()),
                system = args.extractStringsFromAnnotationArgumentEnum(System::class.toSimpleNameString()),
                personenbezogeneDaten = args.extractStringsFromAnnotationArgumentEnum(PersonenbezogeneDaten::class.toSimpleNameString()),
                quellen = args.extractStringsFromAnnotationArgumentString(DsgvoClass::quellen.name) ?: "",
                kategorieVonEmpfaengern = args.extractStringsFromAnnotationArgumentEnumArray(kategorieVonEmpfaengern::class.toSimpleNameString()),
                drittland = args.extractStringsFromAnnotationArgumentBoolean(DsgvoClass::drittland.name) ?: false,
                bemerkungen = args.extractStringsFromAnnotationArgumentString(DsgvoClass::bemerkungen.name) ?: "",
                optionaleTechnischeInformationen = args.extractStringsFromAnnotationArgumentString(DsgvoClass::optionaleTechnischeInformationen.name)
                    ?: ""
            )
        } ?: DsgvoRelevantDataDto()
    }
}