package tech.relaycorp.letro.utils.models

import tech.relaycorp.letro.utils.Logger
import java.lang.Exception

fun createLogger() = object : Logger {
    override fun d(tag: String, message: String) {
        println("D: $tag: $message")
    }

    override fun i(tag: String, message: String) {
        println("I: $tag: $message")
    }

    override fun w(tag: String, exception: Exception) {
        System.err.println("W: $tag: ${exception.message}")
        exception.printStackTrace()
    }

    override fun e(tag: String, message: String, exception: Exception) {
        System.err.println("E: $tag: ${exception.message}")
        exception.printStackTrace()
    }

    override fun println(message: String) {
        kotlin.io.println(message)
    }
}
