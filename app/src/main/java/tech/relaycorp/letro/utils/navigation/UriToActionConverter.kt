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
            else -> null
        }
    }
}

private const val LETRO_HOST = "letro.app"
private const val PATH_PAIR_REQUEST = "/connect/"
private const val FRAGMENT_USER_PREFIX = "u="
