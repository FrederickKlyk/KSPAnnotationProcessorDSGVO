package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel
import de.klyk.annotationprocessorexcel.processor.visitor.DsgvoExportVisitor
import de.klyk.annotationprocessorexcel.processor.visitor.ExcelRow
import org.apache.poi.ss.usermodel.*
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

    private val shouldRun: Boolean = options["runProcessor"]?.toBoolean() ?: false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!shouldRun) {
            logger.warn("shouldRun= $shouldRun, Processor wird vorzeitig ohne Durchlauf beendet!")
            return emptyList()
        }

        logger.warn("Processor started!")
        val symbols = resolver.getSymbolsWithAnnotation(DsgvoExportExcel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
        if(!symbols.any()) {
            logger.warn("No classes with DsgvoExportExcel annotation found!")
            return emptyList()
        }
        val visitor = DsgvoExportVisitor()

        symbols.forEach { it.accept(visitor, Unit) }

        writeCsvExport(visitor.getCsvData())
        createExcelExport(visitor.getExcelData())

        logger.warn("Processor finished!")
        return emptyList()
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

    private fun createExcelExport(excelData: List<ExcelRow>) {
        val workbook: Workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("FrontendDsgvoReport")
        val cellStyle = workbook.createCellStyle().apply {
            setFillForegroundColor(XSSFColor(Color(173, 216, 230), null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        extractDsgvoData(excelData, sheet, cellStyle)
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
        excelData: List<ExcelRow>,
        sheet: Sheet,
        cellStyle: CellStyle
    ) {
        var rowIndex = 0
        excelData.forEach { row ->
            val className = row.className
            val dsgvoInfoData = row.dsgvoInfoData

            sheet.createRow(rowIndex++).apply {
                createCell(0).apply {
                    setCellValue(AnnotationConstants.DATENKLASSE_NAME)
                    this.cellStyle = cellStyle
                }
                createCell(1).setCellValue(className)
            }

            dsgvoInfoData.kategorie.forEach { kategorie ->
                sheet.createRow(rowIndex++).apply {
                    createCell(0).apply {
                        setCellValue(AnnotationConstants.KATEGORIE)
                        this.cellStyle = cellStyle
                    }
                    createCell(1).setCellValue(kategorie)
                }
            }

            dsgvoInfoData.verwendungsZweck.forEach { verwendungszweck ->
                sheet.createRow(rowIndex++).apply {
                    createCell(0).apply {
                        setCellValue(AnnotationConstants.VERWENDUNGSZWECK)
                        this.cellStyle = cellStyle
                    }
                    createCell(1).setCellValue(verwendungszweck)
                }
            }

            sheet.createRow(rowIndex++).apply {
                createCell(0).apply {
                    setCellValue(AnnotationConstants.LAND)
                    this.cellStyle = cellStyle
                }
                createCell(1).setCellValue(dsgvoInfoData.land)
            }

            row.properties.forEach { property ->
                sheet.createRow(rowIndex++).apply {
                    createCell(0).apply {
                        setCellValue(AnnotationConstants.DATENKLASSE_PROPERTY)
                        this.cellStyle = cellStyle
                    }
                    createCell(1).setCellValue(property)
                }
            }

            rowIndex++ // Add an empty row

            (0..1).forEach { sheet.autoSizeColumn(it) }
        }
    }
}