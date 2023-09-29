package tech.relaycorp.letro.utils.files

fun Int.bytesToMb() = this / 1024f / 1024f
fun Int.bytesToKb() = this / 1024f

fun ByteArray.isMoreThanMegabyte(): Boolean =
    this.size.bytesToMb() >= 1

fun ByteArray.isMoreThanKilobyte(): Boolean =
    this.size.bytesToKb() >= 1
