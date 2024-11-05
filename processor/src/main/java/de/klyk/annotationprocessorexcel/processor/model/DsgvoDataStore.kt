package de.klyk.annotationprocessorexcel.processor.model

import com.google.devtools.ksp.processing.KSPLogger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal data class DsgvoDataStore(
    private val rootPath: String,
    val logger: KSPLogger
) {
    private val tempRootDir = File(rootPath)

    private val tempExcelFile = File(rootPath, "dsgvo_data.json")
    private val csvFile = File(rootPath, "dsgvo_data.csv")


    fun appendCsvData(data: String) {
        checkAndMakeDir {
            csvFile.appendText(data)
        }
    }

    fun getCsvData(): String {
        return csvFile.readText()
    }

    private fun checkAndMakeDir(writeBlock: () -> Unit) {
        try {
            if (tempRootDir.exists()) {
                logger.warn("Pfad existiert nicht, Datei wird geschrieben")
                writeBlock.invoke()
            } else {
                tempRootDir.mkdirs().apply {
                    logger.warn("Pfad existiert nicht, Ordner wird erstellt: $this")
                }
                writeBlock.invoke()
            }
        } catch (e: Exception) {
            logger.error("Fehler beim Schreiben der Datei: ${e.message}")
        }
    }

    fun appendExcelData(data: List<ExcelRow>) {
        checkAndMakeDir {
            val existingData = getExcelData().toMutableList()
            existingData.addAll(data)

            tempExcelFile.writeText(Json.encodeToString(existingData))
            logger.warn("Pfad: ${tempExcelFile.absolutePath}, ${tempExcelFile.exists()}")
        }
    }

    fun getExcelData(): List<ExcelRow> {
        try {
            return if (tempExcelFile.exists() && tempExcelFile.length() > 0) {
                Json.decodeFromString(tempExcelFile.readText())
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Fehler beim Lesen der Datei: ${e.message}")
            return emptyList()
        }
    }
}