/*
 * Copyright 2024 Atick Faisal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.atick.core.extensions

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Formats a number to a string with specified decimal places and thousands separators.
 *
 * Handles special values (NaN, Infinity) and provides locale-aware formatting with
 * configurable precision. When used for currency, enforces 2 decimal places and appends
 * the currency symbol.
 *
 * ## Examples
 * ```kotlin
 * 123.4567.format()              // "123.46" (default 2 decimals)
 * 123.4f.format(1)               // "123.4"
 * 123.0.format()                 // "123" (trailing zeros removed)
 * 1234567.89.format()            // "1,234,567.89"
 * 1234.09.format(isCurrency = true)  // "1,234.09$"
 * Double.NaN.format()            // "NaN"
 * Float.POSITIVE_INFINITY.format()   // "∞"
 * ```
 *
 * @param nDecimal Number of decimal places (default: 2). Ignored when isCurrency is true.
 * @param isCurrency If true, formats as currency with 2 decimals and appends currency symbol.
 * @return Formatted string with thousands separators and appropriate decimal precision.
 */
fun <T> T.format(
    nDecimal: Int = 2,
    isCurrency: Boolean = false,
): String where T : Number, T : Comparable<T> {
    return when {
        this.toDouble().isNaN() -> "NaN"
        this.toDouble().isInfinite() -> if (this.toDouble() > 0) "∞" else "-∞"
        else -> {
            val locale = Locale.getDefault()
            val symbols = DecimalFormatSymbols(locale)

            val pattern = if (isCurrency) "#,##0.00" else "#,##0.#"

            val formatted = DecimalFormat(pattern).apply {
                decimalFormatSymbols = symbols
                if (!isCurrency) {
                    maximumFractionDigits = nDecimal
                    minimumFractionDigits = 0
                }
                isGroupingUsed = true
            }.format(this)

            if (isCurrency) {
                val currencySymbol = try {
                    Currency.getInstance(locale).symbol
                } catch (_: IllegalArgumentException) {
                    "$" // Fallback symbol
                }
                "$formatted$currencySymbol"
            } else {
                formatted
            }
        }
    }
}

/**
 * Converts a Unix timestamp (milliseconds) to a human-readable date-time string.
 *
 * The output format is: "MONTH DAY, YEAR at HOUR:MINUTE AM/PM"
 * (e.g., "January 15, 2024 at 3:45 PM")
 *
 * Uses the system's current time zone for conversion.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // Display message timestamp
 * data class Message(val text: String, val timestamp: Long)
 *
 * @Composable
 * fun MessageItem(message: Message) {
 *     Column {
 *         Text(message.text)
 *         Text(
 *             text = message.timestamp.asFormattedDateTime(),
 *             style = MaterialTheme.typography.caption
 *         )
 *     }
 * }
 *
 * // Display last sync time
 * Text("Last synced: ${lastSyncTimestamp.asFormattedDateTime()}")
 *
 * // Display creation date
 * val createdAt = System.currentTimeMillis()
 * Text("Created: ${createdAt.asFormattedDateTime()}")
 * ```
 *
 * ## Examples
 * - `1640995200000L.asFormattedDateTime()` → "December 31, 2021 at 11:59 PM"
 * - `1704067200000L.asFormattedDateTime()` → "January 1, 2024 at 12:00 AM"
 * - `1704110340000L.asFormattedDateTime()` → "January 1, 2024 at 11:59 AM"
 *
 * @receiver Long Unix timestamp in milliseconds (epoch time).
 * @return Formatted date-time string in the format "MONTH DAY, YEAR at HOUR:MINUTE AM/PM".
 *
 * @see kotlinx.datetime.Instant
 * @see kotlinx.datetime.TimeZone
 */
@OptIn(ExperimentalTime::class)
fun Long.asFormattedDateTime(): String {
    val dateTime = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val amPm = if (dateTime.hour < 12) "AM" else "PM"
    val hour = if (dateTime.hour % 12 == 0) 12 else dateTime.hour % 12

    return "${dateTime.month.name} ${dateTime.day}, ${dateTime.year} at $hour:${
        dateTime.minute.toString().padStart(2, '0')
    } $amPm"
}
