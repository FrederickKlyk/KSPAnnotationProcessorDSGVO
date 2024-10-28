package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
import de.klyk.annotationprocessorexcel.processor.visitor.DsgvoExportVisitor
import de.klyk.annotationprocessorexcel.processor.visitor.ExcelRow
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.separatorKomma
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.awt.Color
import java.io.IOException
import kotlin.reflect.KClass

internal class DsgvoExportProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    options: Map<String, String>
) : SymbolProcessor {

    private val shouldRun: Boolean = options["runProcessor"]?.toBoolean() ?: false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!shouldRun) {
            logger.warn("shouldRun= $shouldRun, Processor wird vorzeitig ohne Durchlauf beendet!")
            return emptyList()
        }

        logger.warn("Processor started!")
        val symbols = resolver.findAnnotations(DsgvoClass::class)
        if (symbols.none()) {
            logger.warn("No classes with DsgvoExportExcel annotation found!")
            return emptyList()
        }

        // Visit all classes and delegate in first step visit properties
        val visitor = DsgvoExportVisitor(logger)
        symbols.forEach { classDeclaration ->
            classDeclaration.accept(visitor, Unit)
        }
        val sourceFiles = symbols.mapNotNull {
            logger.warn("Source file: ${it.simpleName.asString()}: ${it.containingFile}")
            it.containingFile
        }.toList()

        writeCsvExport(visitor.getCsvData(), sourceFiles)
        createExcelExport(visitor.getExcelData(), sourceFiles)

        logger.warn("Processor finished!")
        return emptyList()
    }

    private fun Resolver.findAnnotations(kClass: KClass<*>) =
        getSymbolsWithAnnotation(kClass.qualifiedName.toString()).filterIsInstance<KSClassDeclaration>()

    private fun writeCsvExport(csvData: String, sourceFiles: List<KSFile>) {
        try {
            logger.warn("Writing to CSV file...")
            codeGenerator.createNewFile(
                Dependencies(
                    false,
                    *sourceFiles.toTypedArray()
                ),
                "de.klyk.annotationprocessorexcel.generated",
                AnnotationConstants.DSGVO_FILE_NAME,
                "csv"
            ).apply {
                bufferedWriter(Charsets.UTF_8).use { file ->
                    file.write(csvData)
                }
            }
        } catch (e: IOException) {
            logger.warn("Error writing to CSV file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createExcelExport(excelData: List<ExcelRow>, sourceFiles: List<KSFile>) {
        logger.warn("Creating Excel export...")
        val workbook: Workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("FrontendDsgvoReport")
        val cellStyle = workbook.createCellStyle().apply {
            setFillForegroundColor(XSSFColor(Color(173, 216, 230), null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        extractDsgvoData(excelData, sheet, cellStyle)
        writeExcelExport(workbook, sourceFiles)
    }

    private fun extractDsgvoData(
        excelData: List<ExcelRow>,
        sheet: Sheet,
        cellStyle: CellStyle
    ) {
        var rowIndex = 0

        // Create header row
        val headerRow = sheet.createRow(rowIndex++)
        val headers = listOf(
            AnnotationConstants.DATENKLASSE_NAME,
            AnnotationConstants.KATEGORIE,
            AnnotationConstants.VERWENDUNGSZWECK,
            AnnotationConstants.LAND,
            AnnotationConstants.DOMAENE,
            AnnotationConstants.SYSTEM,
            AnnotationConstants.PERSONENBEZOGENE_DATEN,
            AnnotationConstants.QUELLEN,
            AnnotationConstants.KATEGORIE_VON_EMPFAENGERN,
            AnnotationConstants.DRITTLAND,
            AnnotationConstants.BEMERKUNGEN,
            AnnotationConstants.OPTIONALE_TECHNISCHE_INFORMATIONEN
        )

        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                this.cellStyle = cellStyle
            }
        }

        // Create data rows
        excelData.forEach { row ->
            val className = row.className
            val dsgvoRelevantData = row.dsgvoRelevantData

            // Handle DsgvoClass verwendungszweck
            dsgvoRelevantData.verwendungszweck.forEach { verwendungszweck ->
                val dataRow = sheet.createRow(rowIndex++)
                dataRow.createCell(0).setCellValue(className)
                dataRow.createCell(1).setCellValue(dsgvoRelevantData.kategorie.separatorKomma())
                dataRow.createCell(2).setCellValue(verwendungszweck)
                dataRow.createCell(3).setCellValue(dsgvoRelevantData.land)
                dataRow.createCell(4).setCellValue(dsgvoRelevantData.domaene)
                dataRow.createCell(5).setCellValue(dsgvoRelevantData.system)
                dataRow.createCell(6).setCellValue(dsgvoRelevantData.personenbezogeneDaten)
                dataRow.createCell(7).setCellValue(dsgvoRelevantData.quellen)
                dataRow.createCell(8).setCellValue(dsgvoRelevantData.kategorieVonEmpfaengern.separatorKomma())
                dataRow.createCell(9).setCellValue(dsgvoRelevantData.drittland.toString())
                dataRow.createCell(10).setCellValue(dsgvoRelevantData.bemerkungen)
                dataRow.createCell(11).setCellValue(dsgvoRelevantData.optionaleTechnischeInformationen)
            }

            // Handle DsgvoProperty verwendungszweck
            row.dsgvoPropertyRelevantData.forEach { property ->
                property.verwendungszweck.forEach { verwendungszweck ->
                    val dataRow = sheet.createRow(rowIndex++)
                    dataRow.createCell(0).setCellValue(className)
                    dataRow.createCell(1).setCellValue(dsgvoRelevantData.kategorie.separatorKomma())
                    dataRow.createCell(2).setCellValue("$verwendungszweck (${property.name})")
                    dataRow.createCell(3).setCellValue(dsgvoRelevantData.land)
                    dataRow.createCell(4).setCellValue(dsgvoRelevantData.domaene)
                    dataRow.createCell(5).setCellValue(dsgvoRelevantData.system)
                    dataRow.createCell(6).setCellValue(dsgvoRelevantData.personenbezogeneDaten)
                    dataRow.createCell(7).setCellValue(dsgvoRelevantData.quellen)
                    dataRow.createCell(8).setCellValue(dsgvoRelevantData.kategorieVonEmpfaengern.separatorKomma())
                    dataRow.createCell(9).setCellValue(dsgvoRelevantData.drittland.toString())
                    dataRow.createCell(10).setCellValue(dsgvoRelevantData.bemerkungen)
                    dataRow.createCell(11).setCellValue(dsgvoRelevantData.optionaleTechnischeInformationen)
                }
            }
        }

        // Auto-size columns
        (0..11).forEach { sheet.autoSizeColumn(it) }
    }

    private fun writeExcelExport(workbook: Workbook, sourceFiles: List<KSFile>) {
        try {
            logger.warn("Writing to Excel file...")
            val file = codeGenerator.createNewFile(
                Dependencies(
                    false,
                    *sourceFiles.toTypedArray()
                ),
                "de.klyk.annotationprocessorexcel.generated",
                AnnotationConstants.DSGVO_FILE_NAME,
                "xlsx"
            )

            file.use { workbook.write(it) }
        } catch (e: IOException) {
            logger.warn("Error writing to Excel file: ${e.message}")
            e.printStackTrace()
        }
    }
}