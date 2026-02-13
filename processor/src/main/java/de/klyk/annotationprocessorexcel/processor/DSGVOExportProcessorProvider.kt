package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DSGVOExportProcessorProvider : SymbolProcessorProvider {
    /**
     * Für jeden Kompilierungslauf wird eine neue Instanz des Prozessors erstellt.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        DSGVOExportProcessor(environment.codeGenerator, environment.logger, environment.options)
}
