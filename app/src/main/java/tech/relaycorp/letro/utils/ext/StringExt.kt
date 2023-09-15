package tech.relaycorp.letro.utils.ext

import java.net.URLDecoder
import java.net.URLEncoder

fun String?.nullIfBlankOrEmpty() = if (this.isNullOrBlank() || this.isEmpty()) null else this

fun String.isEmptyOrBlank() = isEmpty() || isBlank()

fun String.isNotEmptyOrBlank() = !isEmptyOrBlank()

fun String?.encodeToUTF(): String? {
    if (this == null) {
        return null
    }
    return try {
        URLEncoder.encode(this, Charsets.UTF_8.name())
    } catch (e: Exception) {
        // TODO: log exception?
        null
    }
}

fun String.decodeFromUTF(): String? {
    return try {
        URLDecoder.decode(this, Charsets.UTF_8.name())
    } catch (e: Exception) {
        // TODO: log exception?
        this
    }
}
