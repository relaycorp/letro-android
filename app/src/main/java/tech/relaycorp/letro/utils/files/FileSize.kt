package tech.relaycorp.letro.utils.files

fun Long.bytesToMb() = this / 1024f / 1024f
fun Long.bytesToKb() = this / 1024f

fun Long.isMoreThanMegabyte(): Boolean =
    bytesToMb() >= 1

fun Long.isMoreThanKilobyte(): Boolean =
    bytesToKb() >= 1
