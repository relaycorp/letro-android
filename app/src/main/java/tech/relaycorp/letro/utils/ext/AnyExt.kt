package tech.relaycorp.letro.utils.ext

inline fun <T> T.applyIf(condition: Boolean, block: T.() -> T): T {
    if (condition) {
        return this.block()
    }
    return this
}
