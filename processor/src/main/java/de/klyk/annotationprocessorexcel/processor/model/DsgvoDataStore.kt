package de.klyk.annotationprocessorexcel.processor.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal data class DsgvoDataStore(
    private val rootPath: String
) {

    val tempExcelFile = File("$rootPath\\dsgvo_data.json")
    val tempRootDir = File(rootPath)

    private val csvFile = File("${tempRootDir}\\dsgvo_data.csv")

    fun getPath() = tempExcelFile.absolutePath

    fun deleteFiles() {
        csvFile.delete()
        tempExcelFile.delete()
    }

    fun appendCsvData(data: String) {
        csvFile.appendText(data)
    }

    fun getCsvData(): String {
        return csvFile.readText()
    }

    fun appendExcelData(data: List<ExcelRow>) {
        if (tempExcelFile.exists()) {
            val existingData = getExcelData().toMutableList()
            existingData.addAll(data)
            tempExcelFile.writeText(Json.encodeToString(existingData))
        } else {
            tempExcelFile.mkdirs()
        }
    }

    fun getExcelData(): List<ExcelRow> {
        return if (tempExcelFile.exists() && tempExcelFile.length() > 0) {
            Json.decodeFromString(tempExcelFile.readText())
        } else {
            emptyList()
        }
    }
}