package tech.relaycorp.letro.account.registration.utils

import java.util.Locale
import javax.inject.Inject

interface RegistrationDomainProvider {
    fun getDomain(): String
    fun getDomainLocale(): Locale
}

class RegistrationDomainProviderImpl @Inject constructor() : RegistrationDomainProvider {

    private val lazyDomainLocale: Locale by lazy {
        Locale.getDefault()
    }

    private val lazyDomain: String by lazy {
        DOMAIN_BY_LOCALE[lazyDomainLocale.toString()] ?: FALLBACK_DOMAIN
    }

    override fun getDomain(): String {
        return lazyDomain
    }

    override fun getDomainLocale(): Locale {
        return lazyDomainLocale
    }

    private companion object {
        const val FALLBACK_DOMAIN = "nautilus.ink"
        val DOMAIN_BY_LOCALE = mapOf(
            "en_GB" to "cuppa.fans",
            "en_US" to "applepie.rocks",
            "es_VE" to "guarapo.cafe",
        )
    }
}
