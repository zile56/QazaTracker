package com.zilehasnain.qazatracker.ui.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** "December 15", or "December 15, 2028" once the year isn't the current one. */
fun formatLongDate(date: LocalDate, today: LocalDate): String =
    date.format(dateFormatter(long = true, includeYear = date.year != today.year))

/** "Sep 24", or "Sep 24, 2028" once the year isn't the current one. */
fun formatShortDate(date: LocalDate, today: LocalDate): String =
    date.format(dateFormatter(long = false, includeYear = date.year != today.year))

private fun dateFormatter(long: Boolean, includeYear: Boolean): DateTimeFormatter {
    val month = if (long) "MMMM" else "MMM"
    return DateTimeFormatter.ofPattern(if (includeYear) "$month d, yyyy" else "$month d", Locale.getDefault())
}
