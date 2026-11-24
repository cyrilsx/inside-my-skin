package dev.healthhub.companion.util

import android.util.Log

/**
 * Logging with mandatory redaction. Rule: tokens, secrets, credential values and payload
 * contents are masked before anything is written to logcat — logcat survives in bug reports.
 * Health payloads are never logged; only counts and client ids.
 */
object SafeLog {

    private val SENSITIVE_KEY = Regex("(secret|token|password|passwd|api[-_]?key|personal[-_]?key|authorization|credentials|code|state|client[-_]?id|user[-_]?id)", RegexOption.IGNORE_CASE)
    private val PAIR = Regex("""("[^"]*"|'[^']*'|[A-Za-z0-9_.-]+)(\s*[:=]\s*)("[^"]*"|'[^']*'|[^\s,;&]+)""")
    private val BEARER = Regex("""((?:bearer|token)\s+)[^\s,;&]+""", RegexOption.IGNORE_CASE)

    fun redact(input: String): String {
        if (input.isEmpty()) return input
        return input
            .replace(BEARER, "$1***")
            .replace(PAIR) { m ->
                val key = m.groupValues[1].replace("\"", "").replace("'", "")
                if (!SENSITIVE_KEY.containsMatchIn(key)) return@replace m.value
                val value = m.groupValues[3]
                val masked = if (value.startsWith("\"") || value.startsWith("'")) "${value[0]}***${value[0]}" else "***"
                "${m.groupValues[1]}${m.groupValues[2]}$masked"
            }
    }

    fun d(tag: String, msg: String) = Log.d(tag, redact(msg))

    fun i(tag: String, msg: String) = Log.i(tag, redact(msg))

    fun w(tag: String, msg: String) = Log.w(tag, redact(msg))

    fun e(tag: String, msg: String) = Log.e(tag, redact(msg))

    /** Logs an exception without its message (messages may embed payload excerpts) — class name only. */
    fun e(tag: String, msg: String, t: Throwable) = Log.e(tag, redact(msg) + ": " + t.javaClass.simpleName)
}
