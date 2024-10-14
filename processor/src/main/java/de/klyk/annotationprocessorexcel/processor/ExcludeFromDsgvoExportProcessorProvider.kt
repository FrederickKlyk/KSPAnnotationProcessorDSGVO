package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ExcludeFromDsgvoExportProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ExcludeFromDsgvoExportProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}