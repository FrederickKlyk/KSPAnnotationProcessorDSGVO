package de.klyk.annotationprocessorexcel.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import de.klyk.annotationprocessorexcel.processor.annotations.ExampleAnnotation


class ExampleProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("KSP PROCESS STARTED")
        logger.warn("Processor started!")  // Deutliche Log-Meldung
        // Sucht nach allen Klassen, die mit @ExampleAnnotation annotiert sind
        val symbols = resolver.getSymbolsWithAnnotation(ExampleAnnotation::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        // Logge die gefundenen Klassen
        symbols.forEach { classDeclaration ->
            logger.info("Found class: ${classDeclaration.simpleName.asString()}")
        }

        return emptyList()
    }
}