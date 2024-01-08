package com.ruchitech.quicklinkcaller.helper

import android.util.Log

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Logger(private val tag: String, private val logFile: File) {

    fun logVerbose(message: String) {
        Log.v(tag, message)
        appendLog("VERBOSE: $message")
    }

    fun logDebug(message: String) {
        Log.d(tag, message)
        appendLog("DEBUG: $message")
    }

    fun logInfo(message: String) {
        Log.i(tag, message)
        appendLog("INFO: $message")
    }

    fun logWarning(message: String) {
        Log.w(tag, message)
        appendLog("WARNING: $message")
    }

    fun logError(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
            appendLog("ERROR: $message\n${Log.getStackTraceString(throwable)}")
        } else {
            Log.e(tag, message)
            appendLog("ERROR: $message")
        }
    }

    private fun appendLog(logMessage: String) {
        try {
            val timestamp = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            FileWriter(logFile, true).use { writer ->
                writer.append("$timestamp - $logMessage\n")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error writing to log file", e)
        }
    }
}
