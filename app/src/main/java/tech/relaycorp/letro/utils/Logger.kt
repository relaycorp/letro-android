package tech.relaycorp.letro.utils

import android.util.Log
import tech.relaycorp.letro.BuildConfig
import java.lang.Exception
import javax.inject.Inject

interface Logger {
    fun e(tag: String, message: String, exception: Exception)
    fun w(tag: String, exception: Exception)
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
}

class LoggerImpl @Inject constructor() : Logger {

    override fun e(tag: String, message: String, exception: Exception) {
        Log.e(tag, message, exception)
    }

    override fun d(tag: String, message: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, exception: Exception) {
        Log.w(tag, exception)
    }
}
