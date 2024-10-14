package de.klyk.annotationprocessorexcel.processor


import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class ExcludeFromDsgvoExportProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDsgvoExport")
            .filterIsInstance<KSPropertyDeclaration>()

        // Process the symbols as needed
        symbols.forEach { property ->
            logger.warn("Excluding property: ${property.simpleName.asString()}")
        }

        return emptyList()
    }
}