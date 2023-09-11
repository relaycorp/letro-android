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
            "en_US" -> "@applepie.fans"
            "es_ve" -> "@guarapo.cafe"
            else -> "@nautilus.ink"
        }
    }

    override fun getDomain(): String {
        return lazyDomain
    }
}
