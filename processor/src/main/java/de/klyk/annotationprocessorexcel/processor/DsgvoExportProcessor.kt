package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.ANNOTATION_DSGVO_NAME
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.ANNOTATION_EXCLUDE_FROM_DSGVO_NAME
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.DATENKLASSE_NAME
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.DATENKLASSE_PROPERTY
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.KATEGORIE
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.LAND
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants.VERWENDUNGSZWECK
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel
import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.awt.Color
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter

class DsgvoExportProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    options: Map<String, String>,
) : SymbolProcessor {

    private var shouldRun: Boolean = options["runProcessor"]?.toBoolean() ?: false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!shouldRun){
            logger.warn("shouldRun= $shouldRun, Processor wird vorzeitig ohne Durchlauf beendet!")
            return emptyList()
        }

        logger.warn("Processor started!")
        val symbols = resolver.getSymbolsWithAnnotation(DsgvoExportExcel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.any()) {
            createCsvExport(symbols)
            createExcelExport(symbols)
        }

        logger.warn("Processor finished!")
        return emptyList()
    }

    private fun createCsvExport(symbols: Sequence<KSClassDeclaration>) {
        val csvData = buildString {
            append("$DATENKLASSE_NAME, $DATENKLASSE_PROPERTY, $KATEGORIE,$VERWENDUNGSZWECK,$LAND\n")
            symbols.forEach { classDeclaration ->
                val dsgvoInfoData = classDeclaration.getDsgvoInfoData()
                val className = classDeclaration.simpleName.asString()
                classDeclaration.getAllProperties().forEach { property ->
                    append("$className,${property.simpleName.asString()},${dsgvoInfoData.kategorie},${dsgvoInfoData.verwendungsZweck},${dsgvoInfoData.land}\n")
                }
            }
        }
        writeCsvExport(csvData)
    }

    private fun writeCsvExport(csvData: String) {
        try {
            logger.warn("Writing to CSV file...")
            codeGenerator.createNewFile(
                Dependencies(false),
                "de.klyk.annotationprocessorexcel.generated",
                "DsgvoReportFrontend",
                "csv"
            ).use { file ->
                PrintWriter(OutputStreamWriter(file, "UTF-8")).use { writer ->
                    writer.write(csvData)
                }
            }
        } catch (e: IOException) {
            logger.warn("Error writing to CSV file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createExcelExport(symbols: Sequence<KSClassDeclaration>) {
        val workbook: Workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("FrontendDsgvoReport")
        val cellStyle = workbook.createCellStyle().apply {
            setFillForegroundColor(XSSFColor(Color(173, 216, 230), null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        extractDsgvoData(symbols, sheet, cellStyle)
        writeExcelExport(workbook)
    }

    private fun writeExcelExport(workbook: Workbook) {
        try {
            logger.warn("Writing to Excel file...")
            codeGenerator.createNewFile(
                Dependencies(false),
                "de.klyk.annotationprocessorexcel.generated",
                "DsgvoReportFrontend",
                "xlsx"
            ).use { file ->
                workbook.use { it.write(file) }
            }
        } catch (e: IOException) {
            logger.warn("Error writing to Excel file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun extractDsgvoData(
        symbols: Sequence<KSClassDeclaration>,
        sheet: Sheet,
        cellStyle: CellStyle
    ) {
        var rowIndex = 0
        symbols.forEach { classDeclaration ->
            val dsgvoInfoData = classDeclaration.getDsgvoInfoData()
            val className = classDeclaration.simpleName.asString()

            sheet.createRow(rowIndex++).apply {
                createCell(0).also { cell ->
                    cell.setCellValue(DATENKLASSE_NAME)
                    cell.cellStyle = cellStyle
                }
                createCell(1).setCellValue(className)
            }

            classDeclaration.getAllPropertiesExcluding().forEach { property ->
                sheet.createRow(rowIndex++).apply {
                    createCell(0).also { cell ->
                        cell.setCellValue(DATENKLASSE_PROPERTY)
                        cell.cellStyle = cellStyle
                    }
                    createCell(1).setCellValue(property.simpleName.asString())
                }
            }

            sheet.createRow(rowIndex++).apply {
                createCell(0).also { cell ->
                    cell.setCellValue(KATEGORIE)
                    cell.cellStyle = cellStyle
                }
                createCell(1).setCellValue(dsgvoInfoData.kategorie)
            }

            sheet.createRow(rowIndex++).apply {
                createCell(0).also { cell ->
                    cell.setCellValue(VERWENDUNGSZWECK)
                    cell.cellStyle = cellStyle
                }
                createCell(1).setCellValue(dsgvoInfoData.verwendungsZweck)
            }

            sheet.createRow(rowIndex++).apply {
                createCell(0).also { cell ->
                    cell.setCellValue(LAND)
                    cell.cellStyle = cellStyle
                }
                createCell(1).setCellValue(dsgvoInfoData.land)
            }

            rowIndex++ // Add an empty row

            (0..1).forEach { sheet.autoSizeColumn(it) }
        }
    }

    private fun KSClassDeclaration.getDsgvoInfoData(): DsgvoInfoData {
        val annotation = annotations.find { it.shortName.asString() == ANNOTATION_DSGVO_NAME }

        return annotation?.arguments?.let { args ->
            DsgvoInfoData(
                kategorie = args.find { it.name?.asString() == KATEGORIE.lowercase() }?.value?.toString()?.substringAfterLast('.')
                    ?: Kategorie.BESTANDSKUNDE.name,
                verwendungsZweck = args.find { it.name?.asString() == VERWENDUNGSZWECK.lowercase() }?.value as? String ?: "Datenexport",
                land = args.find { it.name?.asString() == LAND.lowercase() }?.value as? String ?: "Deutschland"
            )
        } ?: DsgvoInfoData()
    }

    private fun KSClassDeclaration.getAllPropertiesExcluding(): Sequence<KSPropertyDeclaration> {
        return getAllProperties().filter { property ->
            !property.annotations.any { it.shortName.asString() == ANNOTATION_EXCLUDE_FROM_DSGVO_NAME }
        }
    }
}

data class DsgvoInfoData(
    var kategorie: String = "",
    var verwendungsZweck: String = "",
    var land: String = ""
)