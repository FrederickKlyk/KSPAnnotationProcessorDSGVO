package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class DsgvoExportProcessorProvider : SymbolProcessorProvider {

    /**
     * Create a new instance of the processor for each compilation run.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DsgvoExportProcessor(environment.codeGenerator, environment.logger, environment.options)
    }
}