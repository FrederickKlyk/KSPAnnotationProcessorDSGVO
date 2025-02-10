package de.klyk.annotationprocessorexcel.processor.annotations

/**
 * Annotation to mark a class as DSGVO relevant.
 * @param solution which solution is leading / responsible for the data.
 * @param system which system (i.e. Frontend, Backend) is currently working with the data.
 * @param datenkategorie The category of the data.
 * @param personenbezogeneDaten The explicit personal data attributes.
 * @param verwendungszweck The purpose of the data processing.
 * @param datenquellen The source of the data.
 * @param kategorieEmpfaenger The category of recipients.
 * @param datenVerschluesselt Whether the data is encrypted. Type of encryption can be added as a technical comment.
 * @param beteiligteLaender The countries involved in the data processing.
 * @param bemerkungen Additional comments.
 * @param optionaleTechnischeInformationen Optional technical information, i.e. encryption type.
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
    val optionaleTechnischeInformationen: String = ""
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

enum class Datenkategorie(override val displayName: String)  : DSGVOEnum {
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

