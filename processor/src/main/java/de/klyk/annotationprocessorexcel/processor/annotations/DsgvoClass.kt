package de.klyk.annotationprocessorexcel.processor.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DsgvoClass(
    val domaene: Domaene,
    val system: SystemCluster,
    val kategorie: Array<Kategorie>,
    val personenbezogeneDaten: PersonenbezogeneDaten,
    val verwendungszweck: Array<Verwendungszweck>,
    val quellen: String,
    val kategorieVonEmpfaengern: Array<kategorieVonEmpfaengern>,
    val drittland: Boolean,
    val land: String,
    val bemerkungen: String = "",
    val optionaleTechnischeInformationen: String = ""
)

enum class kategorieVonEmpfaengern {
    KUNDEN,
    MITARBEITER,
    DIENSTLEISTER
}

enum class PersonenbezogeneDaten {
    JA,
    NEIN
}

enum class SystemCluster {
    FRONTEND,
    BACKEND,
    BACKEND_CLOUD
}

enum class Domaene {
    SALES,
    CRM,
    PIM,
    FINANZEN
}

enum class Kategorie(override val displayName: String)  : DsgvoEnum {
    BESTANDSKUNDE("Bestandskunde"),
    MITARBEITER("Mitarbeiter")
}

enum class Verwendungszweck {
    KUNDENWERBUNG,
    KUNDENBINDUNG,
    LOGGING,
    RECOVERY
}

