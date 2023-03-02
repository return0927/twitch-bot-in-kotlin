package kr.enak.luya.luyasupport.twitch.utils

import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

fun utcNow() = LocalDateTime.now(Clock.systemUTC()).toInstant(ZoneOffset.UTC)

fun Duration.format(): String {
    fun needsPlural(num: Long): String = if (num > 1) "s" else ""
    fun needsPlural(num: Int): String = needsPlural(num.toLong())

    val years = this.toDays() / 365
    val days = this.toDays() % 365
    val hours = this.toHours() % 24
    val minutes = this.toMinutes() % 60
    val seconds = this.toSeconds() % 60

    val parts = mutableListOf<String>(
        "$days day" + needsPlural(days),
        "$hours hour" + needsPlural(hours),
        "$minutes minute" + needsPlural(minutes),
        "$seconds second" + needsPlural(seconds),
    )
    if (years > 0) {
        parts.add(0, "$years year" + if (years > 1) "s" else "")
        parts.removeAt(parts.lastIndex)
    }

    return parts.joinToString(", ")
}

fun Duration.format(format: String): String =
    SimpleDateFormat(format, Locale.KOREA).format(this.toMillis())
