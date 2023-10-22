package tech.relaycorp.letro.utils.collections

fun <K, T> List<T>.toMap(key: (T) -> K): Map<K, T> {
    val result = hashMapOf<K, T>()
    forEach {
        result.put(key(it), it)
    }
    return result
}
