package de.klyk.annotationprocessorexcel.processor.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import de.klyk.annotationprocessorexcel.processor.annotations.Datenkategorie
import de.klyk.annotationprocessorexcel.processor.annotations.DSGVOClass
import de.klyk.annotationprocessorexcel.processor.annotations.DSGVOProperty
import de.klyk.annotationprocessorexcel.processor.annotations.DSGVOPropertyRelevantData
import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDSGVOExport
import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
import de.klyk.annotationprocessorexcel.processor.annotations.Solution
import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
import de.klyk.annotationprocessorexcel.processor.annotations.kategorieEmpfaenger
import de.klyk.annotationprocessorexcel.processor.model.DSGVORelevantDataDto
import de.klyk.annotationprocessorexcel.processor.model.ExcelRow
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractDisplayNameFromAnnotationArgumentEnumArray
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentBoolean
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentEnum
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentEnumArray
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.extractStringsFromAnnotationArgumentString
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.toSimpleNameString

/**
 * the visitor is only responsible for data collection, while the processor handles the creation and extraction of the Excel data,
 * adhering to the separation of concerns principle.
 */
internal class DSGVOExportVisitor(val logger: KSPLogger) : KSVisitorVoid() {
    // Initialize maps for excluded properties and purposes of the classes
    private val purposesMap = mutableMapOf<KSName, MutableList<DSGVOPropertyRelevantData>>()

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
        classDeclaration.annotations.find { it.shortName.asString() == DSGVOClass::class.simpleName }?.let {
            classDeclaration.processDSGVODataExport()
        }
    }

    // Visit all properties of a specific DSGVO annotated class
    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        val className = property.parentDeclaration?.simpleName ?: return
        logger.warn("Processing property in visitPropertyDeclaration: ${property.simpleName.asString()} in class ${className.asString()}")

        property.processDSGVOPropertyPurposeAnnotationData(className)
    }

    fun getCsvData(): String = csvData.toString()

    fun getExcelData(): List<ExcelRow> = excelData

    // Process the purpose annotation data of a property, when available
    private fun KSPropertyDeclaration.processDSGVOPropertyPurposeAnnotationData(className: KSName) {
        purposesMap.putIfAbsent(className, mutableListOf())
        getDSGVOPropertyDataFromDSGVOPropertyAnnotation()?.let {
            purposesMap[className]?.add(it)
        }
    }

    /**
     * Extract the DSGVOProperty annotation data from the property declaration
     */
    private fun KSPropertyDeclaration.getDSGVOPropertyDataFromDSGVOPropertyAnnotation(): DSGVOPropertyRelevantData? {
        return annotations.find { it.shortName.asString() == DSGVOProperty::class.simpleName }?.let {
            DSGVOPropertyRelevantData(
                name = simpleName.asString(),
                verwendungszweck = it.arguments.extractStringsFromAnnotationArgumentEnumArray(DSGVOProperty::verwendungszweckProperty.name)
            )
        }
    }

    private fun KSPropertyDeclaration.isExcluded(): Boolean = annotations.any { it.shortName.asString() == ExcludeFromDSGVOExport::class.simpleName }

    private fun KSClassDeclaration.processDSGVODataExport() {
        val className = simpleName
        val classNameString = simpleName.asString()
        val dsgvoPropertiesFromAnnotation = purposesMap[className] ?: emptyList()
        val dsgvoInfoData = getDsgvoInfoData()

        // Add the class properties to the personenbezogeneDaten
        dsgvoInfoData.personenbezogeneDaten = dsgvoInfoData.personenbezogeneDaten + " (" +
                getAllProperties()
                    .filter { it.isExcluded().not() }
                    .joinToString("; ") { it.simpleName.asString() } + ")"

        // Add the class with its purposes to the csv data
        dsgvoInfoData.verwendungszweck.forEach { verwendungsZweck ->
            csvData.append(dsgvoInfoData.system).append(", ")
                .append(classNameString).append(", ")
                .append("(${dsgvoInfoData.datenkategorie.joinToString(". ")})").append(", ")
                .append(verwendungsZweck).append(", ")
                .append(dsgvoInfoData.beteiligteLaender).append(", ")
                .append(dsgvoInfoData.solution).append(", ")
                .append(dsgvoInfoData.personenbezogeneDaten).append(", ")
                .append(dsgvoInfoData.datenquellen).append(", ")
                .append("(${dsgvoInfoData.kategorieEmpfaenger.joinToString(". ")})").append(", ")
                .append(dsgvoInfoData.datenVerschluesselt).append(", ")
                .append(dsgvoInfoData.bemerkungen).append(", ")
                .append(dsgvoInfoData.optionaleTechnischeInformationen).append("\n")
        }

        // Add the properties with their purposes to the csv data
        dsgvoPropertiesFromAnnotation.forEach { property ->
            property.verwendungszweck.forEach { verwendungsZweck ->
                csvData.append(dsgvoInfoData.system).append(", ")
                    .append(classNameString).append(", ")
                    .append("(${dsgvoInfoData.datenkategorie.joinToString(". ")})").append(", ")
                    .append(verwendungsZweck).append(", ")
                    .append(dsgvoInfoData.beteiligteLaender).append(", ")
                    .append(dsgvoInfoData.solution).append(", ")
                    .append(property.name).append(", ")
                    .append(dsgvoInfoData.datenquellen).append(", ")
                    .append("(${dsgvoInfoData.kategorieEmpfaenger.joinToString(". ")})").append(", ")
                    .append(dsgvoInfoData.datenVerschluesselt).append(", ")
                    .append(dsgvoInfoData.bemerkungen).append(", ")
                    .append(dsgvoInfoData.optionaleTechnischeInformationen).append("\n")
            }
        }

        excelData.add(ExcelRow(classNameString, dsgvoInfoData, dsgvoPropertiesFromAnnotation))
    }

    private fun Sequence<KSPropertyDeclaration>.toPropertyDisplayNames(
        dsgvoProperties: List<DSGVOPropertyRelevantData>
    ): Sequence<String> = map { property ->
        dsgvoProperties.find { it.name == property.simpleName.asString() }?.displayName
            ?: property.simpleName.asString()
    }

    /**
     * Extract the DsgvoClass annotation data from the class declaration
     */
    private fun KSClassDeclaration.getDsgvoInfoData(): DSGVORelevantDataDto {
        return annotations.find { it.shortName.asString() == DSGVOClass::class.simpleName }?.arguments?.let { args ->
            DSGVORelevantDataDto(
                datenkategorie = args.extractDisplayNameFromAnnotationArgumentEnumArray(Datenkategorie::class.toSimpleNameString()),
                verwendungszweck = args.extractStringsFromAnnotationArgumentEnumArray(Verwendungszweck::class.toSimpleNameString()),
                beteiligteLaender = args.extractStringsFromAnnotationArgumentString(DSGVOClass::beteiligteLaender.name) ?: "",
                solution = args.extractStringsFromAnnotationArgumentEnum(Solution::class.toSimpleNameString()),
                system = args.extractStringsFromAnnotationArgumentEnum(System::class.toSimpleNameString()),
                personenbezogeneDaten = args.extractStringsFromAnnotationArgumentEnum(PersonenbezogeneDaten::class.toSimpleNameString()),
                datenquellen = args.extractStringsFromAnnotationArgumentString(DSGVOClass::datenquellen.name) ?: "",
                kategorieEmpfaenger = args.extractStringsFromAnnotationArgumentEnumArray(kategorieEmpfaenger::class.toSimpleNameString()),
                datenVerschluesselt = args.extractStringsFromAnnotationArgumentBoolean(DSGVOClass::datenVerschluesselt.name) ?: false,
                bemerkungen = args.extractStringsFromAnnotationArgumentString(DSGVOClass::bemerkungen.name) ?: "",
                optionaleTechnischeInformationen = args.extractStringsFromAnnotationArgumentString(DSGVOClass::optionaleTechnischeInformationen.name)
                    ?: ""
            )
        } ?: DSGVORelevantDataDto()
    }
}