package de.klyk.annotationprocessorexcel.processor.annotations

/**
 *  Annotation, um eine Klasse als DSGVO-relevant zu kennzeichnen.
 *  @param solution Welche Lösung für die Daten führend / verantwortlich ist.
 *  @param system Welches System (z.B. Frontend, Backend) aktuell mit den Daten arbeitet.
 *  @param datenkategorie Die Kategorie der Daten.
 *  @param personenbezogeneDaten Die expliziten personenbezogenen Datenattribute.
 *  @param verwendungszweck Der Zweck der Datenverarbeitung.
 *  @param datenquellen Die Quelle der Daten.
 *  @param kategorieEmpfaenger Die Kategorie der Empfänger.
 *  @param datenVerschluesselt Ob die Daten verschlüsselt sind.
 *         Die Art der Verschlüsselung kann als technischer Kommentar ergänzt werden.
 *  @param beteiligteLaender Die an der Datenverarbeitung beteiligten Länder.
 *  @param bemerkungen Zusätzliche Anmerkungen.
 *  @param optionaleTechnischeInformationen Optionale technische Informationen, z.B. Verschlüsselungsart.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DSGVOClass(
    val solution: Solution,
    val system: SystemCluster,
    val datenkategorie: Array<Datenkategorie>,
    val personenbezogeneDaten: PersonenbezogeneDaten,
    val verwendungszweck: Array<Verwendungszweck>,
    val datenquellen: String,
    val kategorieEmpfaenger: Array<kategorieEmpfaenger>,
    val datenVerschluesselt: Boolean,
    val beteiligteLaender: String,
    val bemerkungen: String = "",
    val optionaleTechnischeInformationen: String = "",
)

enum class kategorieEmpfaenger {
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

enum class Solution {
    SALES,
    CRM,
    PIM,
    FINANZEN,
    HR
}

enum class Datenkategorie(override val displayName: String) : DSGVOEnum {
    BESTANDSKUNDE("Bestandskunde"),
    MITARBEITER("Mitarbeiter")
}

enum class Verwendungszweck {
    KUNDENWERBUNG,
    KUNDENBINDUNG,
    LOGGING,
    RECOVERY,
    KUNDENVERWALTUNG,
    MARKETING,
    VERTRAGSABWICKLUNG
}
