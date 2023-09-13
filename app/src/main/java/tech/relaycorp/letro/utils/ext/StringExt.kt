package tech.relaycorp.letro.utils.ext

fun String?.nullIfBlankOrEmpty() = if (this.isNullOrBlank() || this.isEmpty()) null else this
