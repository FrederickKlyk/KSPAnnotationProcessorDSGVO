import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.symbolProcessorProviders
import de.klyk.annotationprocessorexcel.processor.DsgvoExportProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Assert.assertEquals
import org.junit.Test

class DsgvoExportProcessorTest {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `test DsgvoExportProcessor generates expected files`() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            symbolProcessorProviders = listOf(DsgvoExportProcessorProvider())
            inheritClassPath = true // Compiled sources have access to classes in your application
            kspArgs = mutableMapOf("runDsgvoProcessor" to "true")
        }
        val result = compilation.compile()

        assertEquals(true, result.messages.contains("Processor started!!"))
        assertEquals(true, result.messages.contains("ExportExcel Argument ist false, kein Export wird durchgeführt!"))
        assertEquals(true, result.messages.contains("Processor finished!"))
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    companion object{
        val source = SourceFile.kotlin(
            "Person.kt", """
            package de.klyk.annotationprocessorexcel.model

            import de.klyk.annotationprocessorexcel.processor.annotations.*

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