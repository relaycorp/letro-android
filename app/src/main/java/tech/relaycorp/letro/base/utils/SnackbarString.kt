package tech.relaycorp.letro.base.utils

import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider

data class SnackbarString(
    @SnackbarStringsProvider.Type val type: Int,
    val persistAfterPageClosing: Boolean = false,
    val args: Array<String> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SnackbarString

        if (type != other.type) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + args.contentHashCode()
        return result
    }
}
