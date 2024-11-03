package de.klyk.annotationprocessorexcel.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
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
    // Initialize maps for excluded properties and purposes of the classes
    private val excludedPropertiesMap = mutableMapOf<KSName, MutableList<String>>()
    private val purposesMap = mutableMapOf<KSName, MutableList<DsgvoPropertyRelevantData>>()

    // Initialize the data for the csv and excel export
    private val csvData = StringBuilder()
    private val excelData = mutableListOf<ExcelRow>()

    // Visit all classes and delegate first properties processing and then class processing
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

    // Visit all properties of a specific dsgvo annotated class
    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        val className = property.parentDeclaration?.simpleName ?: return
        logger.warn("Processing property in visitPropertyDeclaration: ${property.simpleName.asString()} in class ${className.asString()}")

        property.processDsgvoPropertyExcludedAndPurposeAnnotationData(className)
    }

    fun getCsvData(): String = csvData.toString()

    fun getExcelData(): List<ExcelRow> = excelData

    // Process the excluded and purpose annotation data of a property
    private fun KSPropertyDeclaration.processDsgvoPropertyExcludedAndPurposeAnnotationData(className: KSName) {
        // Initialize maps if not already done
        excludedPropertiesMap.putIfAbsent(className, mutableListOf())
        purposesMap.putIfAbsent(className, mutableListOf())
        // Check if the property is excluded
        if (isExcluded()) {
            excludedPropertiesMap[className]?.add(simpleName.asString())
        } else {
            getDsgvoPropertyDataFromDsgvoPropertyAnnotation()?.let {
                purposesMap[className]?.add(it)
            }
        }
    }

    /**
     * Extract the DsgvoProperty annotation data from the property declaration
     */
    private fun KSPropertyDeclaration.getDsgvoPropertyDataFromDsgvoPropertyAnnotation(): DsgvoPropertyRelevantData? {
        return annotations.find { it.shortName.asString() == DsgvoProperty::class.simpleName }?.let {
            DsgvoPropertyRelevantData(
                name = simpleName.asString(),
                verwendungszweck = it.arguments.extractStringsFromAnnotationArgumentEnumArray(DsgvoProperty::verwendungszweckProperty.name)
            )
        }
    }

    private fun KSPropertyDeclaration.isExcluded(): Boolean = annotations.any { it.shortName.asString() == ExcludeFromDsgvoExport::class.simpleName }

    private fun KSClassDeclaration.processDsgvoDataExport() {
        val className = simpleName
        val classNameString = simpleName.asString()
        val excludedProperties = excludedPropertiesMap[className] ?: emptyList()
        val dsgvoPropertiesFromAnnotation = purposesMap[className] ?: emptyList()
        val dsgvoInfoData = getDsgvoInfoData()

        // Add the class properties to the personenbezogeneDaten
        dsgvoInfoData.personenbezogeneDaten = dsgvoInfoData.personenbezogeneDaten + " (" +
                getAllProperties()
                    .filter { it.simpleName.asString() !in excludedProperties }
                    .joinToString(" ") { it.simpleName.asString() } + ")"

        // Add the class with its purposes to the csv data
        dsgvoInfoData.verwendungszweck.forEach { verwendungsZweck ->
            csvData.append(classNameString).append(", ")
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

        // Add the properties with their purposes to the csv data
        dsgvoPropertiesFromAnnotation.forEach { property ->
            property.verwendungszweck.forEach { verwendungsZweck ->
                csvData.append(classNameString).append(", ")
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

        excelData.add(ExcelRow(classNameString, dsgvoInfoData, dsgvoPropertiesFromAnnotation.asSequence()))
    }

    /**
     * Extract the DsgvoClass annotation data from the class declaration
     */
    private fun KSClassDeclaration.getDsgvoInfoData(): DsgvoRelevantDataDto {
        return annotations.find { it.shortName.asString() == DsgvoClass::class.simpleName }?.arguments?.let { args ->
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