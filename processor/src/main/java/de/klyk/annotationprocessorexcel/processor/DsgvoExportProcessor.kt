package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoExportExcel
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.awt.Color
import java.io.IOException
import java.io.OutputStream

class DsgvoExportProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("Processor started!")  // Deutliche Log-Meldung
        val symbols = resolver.getSymbolsWithAnnotation(DsgvoExportExcel::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        if (symbols.any()) {
            createExcelExport(symbols)
        }

        logger.warn("Processor finished!")  // Deutliche Log-Meldung
        return emptyList()
    }

    private fun createExcelExport(symbols: Sequence<KSClassDeclaration>) {
        val workbook: Workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("DsgvoExport")
        val cellStyle = cellStyleBackground(workbook)

        extractDsgvoData(symbols, sheet, cellStyle)
        writeExcelExport(workbook)
    }

    private fun writeExcelExport(workbook: Workbook) {
        try {
            logger.warn("Writing to Excel file...")
            val file: OutputStream = codeGenerator.createNewFile(
                Dependencies(false),
                "de.klyk.annotationprocessorexcel.generated",
                "DsgvoReportFrontend",
                "xlsx"
            )
            workbook.write(file)
            file.close()
            workbook.close()
        } catch (e: IOException) {
            logger.warn("Error writing to Excel file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun extractDsgvoData(
        symbols: Sequence<KSClassDeclaration>,
        sheet: Sheet,
        cellStyle: CellStyle?
    ) {
        var rowIndex = 0
        symbols.forEach { classDeclaration ->
            val annotation = classDeclaration.annotations.find { it.shortName.asString() == "DsgvoExportExcel" }
            val dsgvoInfoData = annotation?.arguments.let { args ->
                val sheetName = args?.find { it.name?.asString() == "sheetName" }?.value as? String ?: "frontend_backup"
                val kategorie = args?.find { it.name?.asString() == "kategorie" }?.value as? String ?: "Kunde"
                val verwendungszweck = args?.find { it.name?.asString() == "verwendungszweck" }?.value as? String ?: "Datenexport"
                val land = args?.find { it.name?.asString() == "land" }?.value as? String ?: "Deutschland"
                DsgvoInfoData(sheetName, kategorie, verwendungszweck, land)
            }

            val className = classDeclaration.simpleName.asString()
            val row = sheet.createRow(rowIndex++)
            val classNameCell = row.createCell(0)
            classNameCell.setCellValue("Class Name")
            classNameCell.cellStyle = cellStyle
            row.createCell(1).setCellValue(className)

            classDeclaration.getAllProperties().forEach { property ->
                val propertyRow = sheet.createRow(rowIndex++)
                val propertyNameCell = propertyRow.createCell(0)
                propertyNameCell.setCellValue("Property Name")
                propertyNameCell.cellStyle = cellStyle
                propertyRow.createCell(1).setCellValue(property.simpleName.asString())
            }

            val kategorieRow = sheet.createRow(rowIndex++)
            val kategorieCell = kategorieRow.createCell(0)
            kategorieCell.setCellValue("Kategorie")
            kategorieCell.cellStyle = cellStyle
            kategorieRow.createCell(1).setCellValue(dsgvoInfoData.kategorie)

            val verwendungszweckRow = sheet.createRow(rowIndex++)
            val verwendungszweckCell = verwendungszweckRow.createCell(0)
            verwendungszweckCell.setCellValue("Verwendungszweck")
            verwendungszweckCell.cellStyle = cellStyle
            verwendungszweckRow.createCell(1).setCellValue(dsgvoInfoData.verwendungsZweck)

            val landRow = sheet.createRow(rowIndex++)
            val landCell = landRow.createCell(0)
            landCell.setCellValue("Land")
            landCell.cellStyle = cellStyle
            landRow.createCell(1).setCellValue(dsgvoInfoData.land)

            // Add an empty row
            rowIndex++

            // Auto-size columns
            for (i in 0..1) {
                sheet.autoSizeColumn(i)
            }
        }
    }

    private fun cellStyleBackground(workbook: Workbook): CellStyle? {
        // Create a cell style with light blue background
        val cellStyle = workbook.createCellStyle()
        val lightBlue = XSSFColor(Color(173, 216, 230), null)
        cellStyle.setFillForegroundColor(lightBlue)
        cellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        return cellStyle
    }
}

data class DsgvoInfoData(
    var sheetName: String = "",
    var kategorie: String = "",
    var verwendungsZweck: String = "",
    var land: String = ""
)