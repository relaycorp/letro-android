package tech.relaycorp.letro.utils.navigation

import android.net.Uri
import tech.relaycorp.letro.ui.navigation.Action
import javax.inject.Inject

interface UriToActionConverter {
    fun convert(uri: String): Action?
}

class UriToActionConverterImpl @Inject constructor() : UriToActionConverter {

    @Suppress("NAME_SHADOWING")
    override fun convert(uri: String): Action? {
        val uri = Uri.parse(uri)
        if (uri.host != LETRO_HOST) {
            return null
        }
        val fragment = uri.fragment
        return when {
            uri.path == PATH_PAIR_REQUEST -> {
                val contactAccountId = if (fragment?.startsWith(FRAGMENT_USER_PREFIX) == true) fragment.removePrefix(FRAGMENT_USER_PREFIX) else ""
                Action.OpenPairRequest(
                    contactAccountId = contactAccountId,
                )
            }
            uri.path == PATH_ACCOUNT_LINKING -> {
                val parameters = fragment?.split("&") ?: return Action.OpenAccountLinking()
                var domain = ""
                var awalaEndpoint = ""
                var token = ""
                parameters.forEach {
                    when {
                        it.startsWith(FRAGMENT_DOMAIN_PREFIX) -> domain = it.removePrefix(FRAGMENT_DOMAIN_PREFIX)
                        it.startsWith(FRAGMENT_AWALA_ENDPOINT_PREFIX) -> awalaEndpoint = it.removePrefix(FRAGMENT_AWALA_ENDPOINT_PREFIX)
                        it.startsWith(FRAGMENT_TOKEN_PREFIX) -> token = it.removePrefix(FRAGMENT_TOKEN_PREFIX)
                    }
                }
                Action.OpenAccountLinking(
                    domain = domain,
                    awalaEndpoint = awalaEndpoint,
                    token = token,
                )
            }
            else -> null
        }
    }
}

private const val LETRO_HOST = "letro.app"

private const val PATH_PAIR_REQUEST = "/connect/"
private const val FRAGMENT_USER_PREFIX = "u="

private const val PATH_ACCOUNT_LINKING = "/account-linking/"
private const val FRAGMENT_DOMAIN_PREFIX = "domain="
private const val FRAGMENT_AWALA_ENDPOINT_PREFIX = "awalaEndpoint="
private const val FRAGMENT_TOKEN_PREFIX = "token="
