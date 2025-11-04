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
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Formats a number (Float or Double) to a string with specified decimal places and thousands separators.
 *
 * This function handles edge cases like NaN and Infinity, provides locale-aware formatting,
 * and automatically removes unnecessary trailing zeros.
 *
 * ## Features
 * - Configurable decimal places (default: 2)
 * - Automatic thousands separators (e.g., 1,234.56)
 * - Removes trailing zeros (123.00 → 123)
 * - Handles special values (NaN → "NaN", ±Infinity → "∞"/"-∞")
 * - Uses dot (.) as decimal separator for consistency
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // In UI - display prices
 * Text("Price: $${price.format(2)}")  // $123.45
 *
 * // Display percentages
 * Text("Success rate: ${(successRate * 100).format(1)}%")  // 95.5%
 *
 * // Display measurements with precision
 * Text("Temperature: ${temperature.format(1)}°C")  // 23.5°C
 *
 * // Display large numbers with thousands separator
 * Text("Downloads: ${downloads.format(0)}")  // 1,234,567
 *
 * // Handle sensor data
 * val reading = sensor.getValue()  // might be NaN if sensor fails
 * Text("Reading: ${reading.format()}")  // Shows "NaN" if invalid
 * ```
 *
 * ## Examples
 * - `123.4567.format()` → "123.46" (default 2 decimals)
 * - `123.4f.format(1)` → "123.4"
 * - `123.0.format()` → "123" (trailing zeros removed)
 * - `(-123.45).format()` → "-123.45"
 * - `1234567.89.format()` → "1,234,567.89" (thousands separator)
 * - `Double.NaN.format()` → "NaN"
 * - `Float.POSITIVE_INFINITY.format()` → "∞"
 * - `Float.NEGATIVE_INFINITY.format()` → "-∞"
 *
 * @receiver T The number to format (must be a Number and Comparable).
 * @param nDecimal Number of decimal places to show (default: 2).
 *                 Use 0 for integers, higher values for more precision.
 *
 * @return Formatted string representation with proper decimal places and thousands separators.
 *
 * @see java.text.DecimalFormat
 */
fun <T> T.format(nDecimal: Int = 2): String where T : Number, T : Comparable<T> {
    return when {
        // Handle special cases for both Float and Double
        this.toDouble().isNaN() -> "NaN"
        this.toDouble().isInfinite() -> if (this.toDouble() > 0) "∞" else "-∞"
        else -> {
            val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                // Ensure consistent decimal separator
                decimalSeparator = '.'
            }

            DecimalFormat("#,##0.#").apply {
                decimalFormatSymbols = symbols
                maximumFractionDigits = nDecimal
                minimumFractionDigits = 0 // Don't force decimal places if not needed
                isGroupingUsed = true // Use thousands separator
            }.format(this)
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
