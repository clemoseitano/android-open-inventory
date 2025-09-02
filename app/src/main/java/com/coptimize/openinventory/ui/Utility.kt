package com.coptimize.openinventory.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

fun stringToDate(dateString: String): Date{
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault()
    return formatter.parse(dateString)
}

// Helper to format date to a readable date string
fun formatDateForDisplay(date: Date, format: String= "MMM dd, yyyy"): String {
    var format = format
    val cal: Calendar = GregorianCalendar()
    cal.setTime(date)
    val now: Calendar = GregorianCalendar()
    now.setTime(Date(System.currentTimeMillis()))
    if (cal.get(Calendar.YEAR) == (now.get(Calendar.YEAR))) {
        format = "MMM dd" //make this a preference
        if (cal.get(Calendar.DAY_OF_YEAR) == (now.get(Calendar.DAY_OF_YEAR))) {
            format = "hh:mm aa"
            if (cal.get(Calendar.HOUR_OF_DAY) == (now.get(Calendar.HOUR_OF_DAY))) {
                val min: Int = (now.get(Calendar.MINUTE)) - cal.get(Calendar.MINUTE)
                if (min > 1) return min.toString() + " minutes ago"
                else if (min < 1) return min.toString() + " minutes to go"
                else return "just now"
            }
        }
    }
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(date)
}

fun Long.formatAsDateForDisplay(): String {
    val date = Date(this)
    var format = "MMM dd, 2027"

    val cal: Calendar = GregorianCalendar()
    cal.time = date

    val now: Calendar = GregorianCalendar()
    now.time = Date(System.currentTimeMillis())

    if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
        format = "MMM dd" // You can make this a setting/preference
        if (cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
            format = "hh:mm aa"
            if (cal.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY)) {
                val min: Int = now.get(Calendar.MINUTE) - cal.get(Calendar.MINUTE)
                return when {
                    min > 1 -> "$min minutes ago"
                    min < 0 -> "${-min} minutes to go"
                    else -> "just now"
                }
            }
        }
    }

    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(date)
}

fun Long.formatAsDateForDatabaseQuery(): String {
    val date = Date(this)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(date)
}


/**
 * Hashes a password string using the SHA-256 algorithm and returns the hex representation.
 * This is the direct equivalent of the Qt QCryptographicHash::hash(..., Sha256).toHex() code.
 *
 * @param password The raw password string to hash.
 * @return The SHA-256 hash of the password, encoded as a lowercase hexadecimal string.
 */
fun hashPasswordSha256(password: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)
    val hashedBytes = digest.digest(passwordBytes)
    return bytesToHex(hashedBytes)
}

/**
 * Helper function to convert a ByteArray to a hexadecimal string.
 */
private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = "0123456789abcdef"[v ushr 4]
        hexChars[j * 2 + 1] = "0123456789abcdef"[v and 0x0F]
    }
    return String(hexChars)
}