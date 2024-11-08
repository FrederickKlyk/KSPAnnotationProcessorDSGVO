package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.validate
import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants
import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
import de.klyk.annotationprocessorexcel.processor.model.DsgvoDataStore
import de.klyk.annotationprocessorexcel.processor.model.ExcelRow
import de.klyk.annotationprocessorexcel.processor.visitor.DsgvoExportVisitor
import de.klyk.annotationprocessorexcel.processor.visitor.helper.VisitorExtractHelper.separatorKommaForExport
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

    private val runDsgvoProcessor: Boolean = options["runDsgvoProcessor"]?.toBoolean() ?: false
    private val exportExcel: Boolean = options["exportDsgvoExcel"]?.toBoolean() ?: false
    private val bufferFilePath = "${options["project.root"]}/build/ksp-exports"
    private val dsgvoDataStore = DsgvoDataStore(bufferFilePath, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!runDsgvoProcessor) {
            logger.warn("runDsgvoProcessor: $runDsgvoProcessor, Processor wird vorzeitig ohne Durchlauf beendet!")
            return emptyList()
        }
        logger.warn("Processor started!!")
        logger.warn("Excel Buffer File Path: $bufferFilePath")

        val symbolsDsgvo = resolver.findAnnotations(DsgvoClass::class)
        if (symbolsDsgvo.none()) {
            logger.warn("No classes with DsgvoClass annotation found!")
            return emptyList()
        }

        // Visit all classes and delegate in first step visit properties
        val visitor = DsgvoExportVisitor(logger)
        symbolsDsgvo.forEach { classDeclaration ->
            classDeclaration.accept(visitor, Unit)
        }
        /**
         * Für jedes Symbol wird die zugehörige Quelldatei ermittelt und in einer Liste gespeichert.
         * Wird für die Dependencies in der CodeGenerator.createNewFile-Methode benötigt (inkrementelle Kompilierungsstrategie).
         */
        val sourceFiles = symbolsDsgvo.mapNotNull {
            logger.warn("Source file: ${it.simpleName.asString()}: ${it.containingFile}")
            it.containingFile
        }.toList()

        dsgvoDataStore.appendCsvData(visitor.getCsvData())
        dsgvoDataStore.appendExcelData(visitor.getExcelData())

        if (exportExcel) {
            writeCsvExport(dsgvoDataStore.getCsvData(), sourceFiles)
            createExcelExport(dsgvoDataStore.getExcelData(), sourceFiles)
        } else {
            logger.warn("ExportExcel Argument ist $exportExcel, kein Export wird durchgeführt!")
        }
        logger.warn("Processor finished!")

        /**
         * Beispiele für ungültige Symbole:
         * Fehlende Annotationen: Das Symbol hat nicht die erforderlichen Annotationen.
         * Ungültige Syntax: Das Symbol enthält Syntaxfehler oder ist unvollständig.
         * Falscher Typ: Das Symbol ist nicht der erwartete Typ (z.B. eine Klasse statt einer Methode).
         * Fehlende Abhängigkeiten: Das Symbol hängt von anderen Symbolen ab, die nicht vorhanden oder nicht korrekt sind.
         */
        return symbolsDsgvo.filterNot { it.validate() }.toList()
    }

    /**
     * Finde alle Symbole mit der angegebenen Klassen-Nnnotation.
     */
    private fun Resolver.findAnnotations(kClass: KClass<*>) =
        getSymbolsWithAnnotation(kClass.qualifiedName.toString()).filterIsInstance<KSClassDeclaration>()

    /**
     * Bereite den Excelreport inklusive Styling vor und führe final den Excelexport durch.
     */
    private fun createExcelExport(excelData: List<ExcelRow>, sourceFiles: List<KSFile>) {
        logger.warn("Creating Excel export...")
        // Create Excel workbook and sheet
        val workbook: Workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("FrontendDsgvoReport")
        val cellStyle = workbook.createCellStyle().apply {
            setFillForegroundColor(XSSFColor(Color(173, 216, 230), null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        extractDsgvoData(excelData, sheet, cellStyle)
        writeExcelExport(workbook, sourceFiles)
    }

    /**
     * Extrahiere die DSGVO-Daten aus den Excel-Rohdaten und schreibe sie in die Excel-Exportdatei.
     */
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
                var cellCount = 0
                val dataRow = sheet.createRow(rowIndex++)
                dataRow.apply {
                    createCell(cellCount++).setCellValue(className)
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.kategorie.separatorKommaForExport())
                    createCell(cellCount++).setCellValue(verwendungszweck)
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.land)
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.domaene)
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.system)
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.personenbezogeneDaten)
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.quellen)
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.kategorieVonEmpfaengern.separatorKommaForExport())
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.drittland.toString())
                    createCell(cellCount++).setCellValue(dsgvoRelevantData.bemerkungen)
                    createCell(cellCount).setCellValue(dsgvoRelevantData.optionaleTechnischeInformationen)
                }
            }

            // Handle DsgvoProperty verwendungszweck
            row.dsgvoPropertyRelevantData.forEach { property ->
                property.verwendungszweck.forEach { verwendungszweck ->
                    var cellCount = 0
                    val dataRow = sheet.createRow(rowIndex++)
                    dataRow.apply {
                        createCell(cellCount++).setCellValue(className)
                        createCell(cellCount++).setCellValue(dsgvoRelevantData.kategorie.separatorKommaForExport())
                        createCell(cellCount++).setCellValue(verwendungszweck)
                        createCell(cellCount++).setCellValue(dsgvoRelevantData.land)
                        createCell(cellCount++).setCellValue(dsgvoRelevantData.domaene)
                        createCell(cellCount++).setCellValue(dsgvoRelevantData.system)
                        createCell(cellCount++).setCellValue(property.name)
                        createCell(cellCount++).setCellValue(dsgvoRelevantData.quellen)
                        createCell(cellCount++).setCellValue(dsgvoRelevantData.kategorieVonEmpfaengern.separatorKommaForExport())
                        createCell(cellCount++).setCellValue(dsgvoRelevantData.drittland.toString())
                        createCell(cellCount++).setCellValue(dsgvoRelevantData.bemerkungen)
                        createCell(cellCount).setCellValue(dsgvoRelevantData.optionaleTechnischeInformationen)
                    }
                }
            }
        }

        // Auto-size columns
        (0..11).forEach { sheet.autoSizeColumn(it) }
    }

    /**
     * Schreibe den Excel-Export.
     */
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

    /**
     * Schreibe die CSV-Datei.
     */
    private fun writeCsvExport(csvData: String, sourceFiles: List<KSFile>) {
        try {
            logger.warn("Writing to CSV file...")
            /**
             * Aggregating: Wenn ein Processor aggregiert ist, bezieht sich jede Änderung an einem der Eingaben auf alle verarbeiteten Dateien
             * und triggert eine erneute Verarbeitung aller abhängigen Dateien. Dies eignet sich für globale Ausgaben, wie eine Datei,
             * die Daten aus mehreren Klassen oder Modulen kombiniert. (aggregating = true)
             *
             * Isolating: Ein isolierender Processor hat spezifische Eingaben-Ausgaben-Zuordnungen. Änderungen an einer Datei wirken sich nur auf
             * direkt abhängige Ausgaben aus. (aggregating = false)
             *
             * sources: Eine Liste von KSFile, die die Abhängigkeiten definiert. Änderungen an diesen Dateien werden den inkrementellen Build auslösen.
             */
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
}