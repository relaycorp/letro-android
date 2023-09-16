package tech.relaycorp.letro.onboarding.registration

import java.util.Locale
import javax.inject.Inject

interface RegistrationDomainProvider {
    fun getDomain(): String
}

class RegistrationDomainProviderImpl @Inject constructor() : RegistrationDomainProvider {

    private val lazyDomain: String by lazy {
        val locale = Locale.getDefault()
        when (locale.toString()) {
            "en_GB" -> "@cuppa.fans"
            "en_US" -> "@applepie.rocks"
            "es_VE" -> "@guarapo.cafe"
            else -> "@nautilus.ink"
        }
    }

    override fun getDomain(): String {
        return lazyDomain
    }
}
