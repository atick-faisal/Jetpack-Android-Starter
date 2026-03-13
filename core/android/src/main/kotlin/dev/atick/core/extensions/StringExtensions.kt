/*
 * Copyright 2023 Atick Faisal
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

import android.util.Patterns
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Validates if this string is a properly formatted email address.
 *
 * Uses Android's built-in `Patterns.EMAIL_ADDRESS` for validation, which checks for:
 * - Proper email format (local-part@domain)
 * - Valid characters in local and domain parts
 * - At least one dot in the domain
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // In a ViewModel - validate user input
 * fun validateEmail(email: String): Boolean {
 *     return email.isEmailValid()
 * }
 *
 * // In a Composable - show error state
 * var email by remember { mutableStateOf("") }
 * var isError by remember { mutableStateOf(false) }
 *
 * OutlinedTextField(
 *     value = email,
 *     onValueChange = {
 *         email = it
 *         isError = !it.isEmailValid()
 *     },
 *     isError = isError,
 *     label = { Text("Email") }
 * )
 *
 * // Null-safe usage
 * val userEmail: String? = getUserEmail()
 * if (userEmail.isEmailValid()) {
 *     sendEmail(userEmail!!)
 * }
 * ```
 *
 * ## Valid Examples
 * - "user@example.com" → true
 * - "john.doe@company.co.uk" → true
 * - "test+filter@gmail.com" → true
 *
 * ## Invalid Examples
 * - null → false
 * - "" → false
 * - "notanemail" → false
 * - "@example.com" → false
 * - "user@" → false
 *
 * @receiver String? The email string to validate (can be null).
 * @return true if the string is a valid email address, false otherwise (including null/empty).
 *
 * @see android.util.Patterns.EMAIL_ADDRESS
 */
fun String?.isEmailValid(): Boolean {
    return !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Validates if this string meets password complexity requirements.
 *
 * ## Current Requirements
 * - At least one digit (0-9)
 * - At least one lowercase letter (a-z)
 * - Length between 8 and 20 characters
 *
 * **Note:** Adjust the regex pattern if you need different requirements (uppercase, special chars, etc.).
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // In a ViewModel - validate password on signup
 * fun validatePassword(password: String): PasswordValidation {
 *     return when {
 *         password.isEmpty() -> PasswordValidation.Empty
 *         !password.isPasswordValid() -> PasswordValidation.Weak
 *         else -> PasswordValidation.Valid
 *     }
 * }
 *
 * // In a Composable - show password strength
 * var password by remember { mutableStateOf("") }
 * val isValid = password.isPasswordValid()
 *
 * OutlinedTextField(
 *     value = password,
 *     onValueChange = { password = it },
 *     isError = password.isNotEmpty() && !isValid,
 *     supportingText = {
 *         if (password.isNotEmpty() && !isValid) {
 *             Text("Password must be 8-20 chars with a digit and lowercase letter")
 *         }
 *     }
 * )
 * ```
 *
 * ## Valid Examples
 * - "password1" → true (8+ chars, has digit and lowercase)
 * - "myp4ssword" → true
 * - "test1234567890" → true
 *
 * ## Invalid Examples
 * - null → false
 * - "" → false
 * - "short1" → false (< 8 chars)
 * - "password" → false (no digit)
 * - "12345678" → false (no lowercase letter)
 * - "verylongpasswordwithmorethan20characters1" → false (> 20 chars)
 *
 * @receiver String? The password string to validate (can be null).
 * @return true if the password meets complexity requirements, false otherwise.
 */
fun String?.isPasswordValid(): Boolean {
    val passwordRegex = "^(?=.*\\d)(?=.*[a-z]).{8,20}$"
    val pattern: Pattern = Pattern.compile(passwordRegex)
    val matcher: Matcher = pattern.matcher(this ?: "")
    return matcher.matches()
}

/**
 * Validates if this string represents a valid full name.
 *
 * ## Validation Rules
 * - Must contain at least two parts (first name and last name) separated by spaces
 * - Each part must contain only letters (no numbers or special characters)
 * - Null or empty strings are considered invalid
 *
 * **Note:** This is a basic validation. Real-world names can include:
 * - Hyphens (e.g., "Mary-Jane")
 * - Apostrophes (e.g., "O'Brien")
 * - Accented characters (e.g., "José", "Françoise")
 * - Single names (mononyms)
 *
 * Adjust validation logic based on your application's requirements and target audience.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // In a signup form ViewModel
 * fun validateFullName(name: String): Boolean {
 *     return name.isValidFullName()
 * }
 *
 * // In a Composable
 * var fullName by remember { mutableStateOf("") }
 * val isValid = fullName.isValidFullName()
 *
 * OutlinedTextField(
 *     value = fullName,
 *     onValueChange = { fullName = it },
 *     isError = fullName.isNotEmpty() && !isValid,
 *     label = { Text("Full Name") },
 *     supportingText = {
 *         if (fullName.isNotEmpty() && !isValid) {
 *             Text("Enter first and last name")
 *         }
 *     }
 * )
 * ```
 *
 * ## Valid Examples
 * - "John Doe" → true
 * - "Mary Jane Watson" → true (three parts is fine)
 * - "Alice Smith Johnson" → true
 *
 * ## Invalid Examples
 * - null → false
 * - "" → false
 * - "John" → false (only one part)
 * - "John123" → false (contains numbers)
 * - "John-Doe" → false (contains hyphen - not a letter)
 * - "John Doe!" → false (contains special character)
 *
 * @receiver String? The full name string to validate (can be null).
 * @return true if the string represents a valid full name with at least first and last name,
 *         false otherwise.
 */
fun String?.isValidFullName(): Boolean {
    if (this == null) return false
    // Split the full name into parts using spaces as separators
    val parts = split(" ")

    // Check if there are at least two parts (first name and last name)
    if (parts.size < 2) return false

    // Check if each part contains only letters (assuming names don't contain special characters)
    for (part in parts) {
        if (!part.all { it.isLetter() }) {
            return false
        }
    }

    return true
}
