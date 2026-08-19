package com.mountsa.fmsimulation.ui.screens.dashboard.components

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Mendapatkan nama hari dari timestamp
 * @param timestamp waktu dalam milliseconds
 * @return nama hari (Monday, Tuesday, etc)
 */

fun getDayName(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        Calendar.SUNDAY -> "Sunday"
        else -> "Unknown"
    }
}

/**
 * Mendapatkan format tanggal pendek (DD/MM/YYYY)
 * @param timestamp waktu dalam milliseconds
 * @return format tanggal "DD/MM/YYYY"
 */
fun getDateString(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

/**
 * Mendapatkan format tanggal panjang dengan bulan
 * @param timestamp waktu dalam milliseconds
 * @return format tanggal "DD Month YYYY"
 */
fun getLongDateString(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

/**
 * Format tanggal pendek untuk fixture card (DD MMM)
 * @param timestamp waktu dalam milliseconds
 * @return format "DD MMM"
 */
fun formatDateShort(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

/**
 * Mendapatkan waktu relatif (Today, Tomorrow, Yesterday)
 * @param timestamp waktu dalam milliseconds
 * @return string relatif
 */
fun getRelativeDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffInDays = TimeUnit.MILLISECONDS.toDays(timestamp - now)

    return when (diffInDays) {
        0L -> "Today"
        1L -> "Tomorrow"
        -1L -> "Yesterday"
        else -> getDateString(timestamp)
    }
}

/**
 * Mendapatkan nama bulan dari timestamp
 */
fun getMonthName(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

/**
 * Mendapatkan tahun dari timestamp
 */
fun getYear(timestamp: Long): Int {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    return calendar.get(Calendar.YEAR)
}