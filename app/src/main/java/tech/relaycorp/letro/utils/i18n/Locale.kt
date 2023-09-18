package tech.relaycorp.letro.utils.i18n

import java.util.Locale

fun Locale.normaliseString(): String {
    val languageCode = language.lowercase()
    val countryCode = country.lowercase()
    return if (languageCode.isEmpty()) {
        ""
    } else if (countryCode.isEmpty()) {
        languageCode
    } else {
        "$languageCode-$countryCode"
    }
}

fun String.parseLocale(): Locale {
    val localeParts = split("-")
    val languageCode = localeParts[0].lowercase()
    val countryCode = localeParts.getOrNull(1)?.uppercase() ?: ""
    val variantCode = localeParts.getOrNull(2) ?: ""
    return Locale(languageCode, countryCode, variantCode)
}
