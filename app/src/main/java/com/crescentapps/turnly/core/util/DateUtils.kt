package com.crescentapps.turnly.core.util

import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Currency
import java.util.Locale

object DateUtils {
    private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): LocalDate = LocalDate.now()

    fun todayString(): String = today().format(DATE_FORMATTER)

    fun parse(dateStr: String): LocalDate = LocalDate.parse(dateStr, DATE_FORMATTER)

    fun format(date: LocalDate): String = date.format(DATE_FORMATTER)

    fun formatDisplay(dateStr: String): String {
        return runCatching {
            val date = parse(dateStr)
            val today = today()
            when {
                date == today -> "Today · ${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
                date == today.plusDays(1) -> "Tomorrow · ${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
                date == today.minusDays(1) -> "Yesterday · ${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
                date.year == today.year -> "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
                else -> "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${date.year}"
            }
        }.getOrDefault(dateStr)
    }

    fun formatShortDisplay(dateStr: String): String {
        return runCatching {
            val date = parse(dateStr)
            "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
        }.getOrDefault(dateStr)
    }

    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.DAYS.between(start, end)
    }
}

object CurrencyUtils {
    fun formatAmount(amount: Double, currencyCode: String = "INR"): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
            format.currency = currency
            if (amount % 1.0 == 0.0) {
                format.maximumFractionDigits = 0
            }
            format.format(amount)
        } catch (e: Exception) {
            val symbol = when (currencyCode) {
                "INR" -> "₹"
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                else -> currencyCode
            }
            if (amount % 1.0 == 0.0) {
                "$symbol${amount.toLong()}"
            } else {
                "$symbol${String.format(Locale.getDefault(), "%.2f", amount)}"
            }
        }
    }
}
