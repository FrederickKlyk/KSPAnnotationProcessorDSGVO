package de.klyk.annotationprocessorexcel.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class DsgvoExportProcessorTest {

    @AfterEach
    fun cleanUpAfterTest() {
        val bufferFileCsv = File("build/testbuild/build/ksp-exports/dsgvo_data.csv")
        val bufferFileJson = File("build/testbuild/build/ksp-exports/dsgvo_data.json")
        if (bufferFileCsv.exists()) bufferFileCsv.delete()
        if (bufferFileJson.exists()) bufferFileJson.delete()
        println("Buffer files cleared!")
    }

    @Test
    fun `test DsgvoExportProcessor generates csv and excel export`() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            symbolProcessorProviders = listOf(DsgvoExportProcessorProvider())
            inheritClassPath = true // Compiled sources have access to classes in your application
            kspArgs = mutableMapOf("runDsgvoProcessor" to "true", "exportDsgvoExcel" to "true", "project.root" to "build/testbuild")
        }
        val result = compilation.compile()

        assertEquals(true, result.messages.contains("Processor started!!"))
        assertEquals(true, result.messages.contains("Writing to CSV file..."))
        assertEquals(true, result.messages.contains("Creating Excel export..."))
        assertEquals(true, result.messages.contains("Processor finished!"))
        assertEquals(true, result.messages.contains("round 2 of processing"))
        assertEquals(true, result.messages.contains("No classes with DsgvoClass annotation found!"))
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `test DsgvoExportProcessor generates buffer files but no csv and excel export`() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            symbolProcessorProviders = listOf(DsgvoExportProcessorProvider())
            inheritClassPath = true // Compiled sources have access to classes in your application
            kspArgs = mutableMapOf("runDsgvoProcessor" to "true", "exportDsgvoExcel" to "false", "project.root" to "build/testbuild")
        }
        val result = compilation.compile()

        assertEquals(true, result.messages.contains("Processor started!!"))
        assertEquals(true, result.messages.contains("Excel Buffer File Path: build/testbuild/build/ksp-exports"))
        assertEquals(true, result.messages.contains("ExportExcel Argument ist false, kein Export wird durchgeführt!"))
        assertEquals(true, result.messages.contains("Processor finished!"))
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `test DsgvoExportProcessor stop running when runDsgvoProcessor is false `() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            symbolProcessorProviders = listOf(DsgvoExportProcessorProvider())
            inheritClassPath = true // Compiled sources have access to classes in your application
            kspArgs = mutableMapOf("runDsgvoProcessor" to "false", "exportDsgvoExcel" to "false", "project.root" to "build/testbuild")
        }
        val result = compilation.compile()

        assertEquals(true, result.messages.contains("runDsgvoProcessor: false, Processor wird vorzeitig ohne Durchlauf beendet!"))
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    companion object {
        val source = SourceFile.kotlin(
            "Person.kt", """
            import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
            import de.klyk.annotationprocessorexcel.processor.annotations.AnnotationConstants
            import de.klyk.annotationprocessorexcel.processor.annotations.Domaene
            import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoClass
            import de.klyk.annotationprocessorexcel.processor.annotations.DsgvoProperty
            import de.klyk.annotationprocessorexcel.processor.annotations.ExcludeFromDsgvoExport
            import de.klyk.annotationprocessorexcel.processor.annotations.Kategorie
            import de.klyk.annotationprocessorexcel.processor.annotations.PersonenbezogeneDaten
            import de.klyk.annotationprocessorexcel.processor.annotations.SystemCluster
            import de.klyk.annotationprocessorexcel.processor.annotations.Verwendungszweck
            import de.klyk.annotationprocessorexcel.processor.annotations.kategorieVonEmpfaengern

            @DsgvoClass(
                kategorie = [Kategorie.BESTANDSKUNDE],
                verwendungszweck = [Verwendungszweck.LOGGING, Verwendungszweck.RECOVERY],
                land = "DE, FR, AT",
                domaene = Domaene.FINANZEN,
                system = SystemCluster.FRONTEND,
                personenbezogeneDaten = PersonenbezogeneDaten.JA,
                quellen = "partiendo",
                kategorieVonEmpfaengern = [kategorieVonEmpfaengern.KUNDEN],
                drittland = false,
                bemerkungen = "laoreet",
                optionaleTechnischeInformationen = "keine",
            )
            data class Person(
                val name: String,
                val age: Int,
                val phoneNumber: Number,
                @DsgvoProperty([Verwendungszweck.KUNDENWERBUNG, Verwendungszweck.KUNDENBINDUNG])
                val email: String,
                @ExcludeFromDsgvoExport
                val irrelevantInfo: String,
                val relevanteInfo: String
            )
        """
        )
    }
}