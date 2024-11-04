package de.klyk.annotationprocessorexcel.processor.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object DsgvoDataStore {
    private val csvFile = File("dsgvo_data.csv")
    private val tempExcelFile = File("dsgvo_data.json")

    fun appendCsvData(data: String) {
        csvFile.appendText(data)
    }

    fun getCsvData(): String {
        return csvFile.readText()
    }

    fun appendExcelData(data: List<ExcelRow>) {
        val existingData = getExcelData().toMutableList()
        existingData.addAll(data)
        tempExcelFile.writeText(Json.encodeToString(existingData))
    }

    fun getExcelData(): List<ExcelRow> {
        return if (tempExcelFile.exists() && tempExcelFile.length() > 0) {
            Json.decodeFromString(tempExcelFile.readText())
        } else {
            emptyList()
        }
    }
}