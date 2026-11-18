package com.weatherwise.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateFormatter {

    fun formatCurrentDateTime(timestampSeconds: Long, timezoneOffsetSeconds: Int = 0): String {
        val date = Date(timestampSeconds * 1000L)
        val sdf = SimpleDateFormat("EEEE, MMM d, h:mm a", Locale.getDefault())
        if (timezoneOffsetSeconds != 0) {
            sdf.timeZone = TimeZone.getTimeZone(String.format(Locale.US, "GMT%+d", timezoneOffsetSeconds / 3600))
        }
        return sdf.format(date)
    }

    fun formatHour(timestampSeconds: Long, timezoneOffsetSeconds: Int = 0): String {
        val date = Date(timestampSeconds * 1000L)
        val sdf = SimpleDateFormat("h a", Locale.getDefault())
        if (timezoneOffsetSeconds != 0) {
            sdf.timeZone = TimeZone.getTimeZone(String.format(Locale.US, "GMT%+d", timezoneOffsetSeconds / 3600))
        }
        return sdf.format(date)
    }

    fun formatDayName(timestampSeconds: Long, timezoneOffsetSeconds: Int = 0): String {
        val date = Date(timestampSeconds * 1000L)
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        if (timezoneOffsetSeconds != 0) {
            sdf.timeZone = TimeZone.getTimeZone(String.format(Locale.US, "GMT%+d", timezoneOffsetSeconds / 3600))
        }
        return sdf.format(date)
    }

    fun formatShortDayAndDate(timestampSeconds: Long, timezoneOffsetSeconds: Int = 0): String {
        val date = Date(timestampSeconds * 1000L)
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        if (timezoneOffsetSeconds != 0) {
            sdf.timeZone = TimeZone.getTimeZone(String.format(Locale.US, "GMT%+d", timezoneOffsetSeconds / 3600))
        }
        return sdf.format(date)
    }

    fun formatTimeOnly(timestampSeconds: Long, timezoneOffsetSeconds: Int = 0): String {
        val date = Date(timestampSeconds * 1000L)
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        if (timezoneOffsetSeconds != 0) {
            sdf.timeZone = TimeZone.getTimeZone(String.format(Locale.US, "GMT%+d", timezoneOffsetSeconds / 3600))
        }
        return sdf.format(date)
    }

    fun formatRelativeTime(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return "Never"
        val diffSeconds = (System.currentTimeMillis() - timestampMillis) / 1000
        return when {
            diffSeconds < 60 -> "Just now"
            diffSeconds < 3600 -> "${diffSeconds / 60} min ago"
            diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                sdf.format(Date(timestampMillis))
            }
        }
    }
}
