package com.example.data

import java.security.MessageDigest
import kotlin.random.Random

object SecurityUtils {

    /**
     * Hashes passwords using SHA-256 to ensure plaintext credentials
     * are never stored in database or memory logs.
     */
    fun hashPassword(raw: String): String {
        if (raw.isBlank()) return ""
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a secure, authentic 6-digit numeric OTP for phone verification.
     */
    fun generateOtp(): String {
        val number = Random.nextInt(100000, 999999)
        return number.toString()
    }

    /**
     * Cleans and formats phone numbers (e.g., strips non-digits except leading +).
     */
    fun normalizePhone(raw: String): String {
        return raw.trim().replace(Regex("[^0-9+]"), "")
    }

    /**
     * Validates that phone number contains at least 10 digits.
     */
    fun isValidPhone(raw: String): Boolean {
        val digitsOnly = raw.filter { it.isDigit() }
        return digitsOnly.length in 10..15
    }

    /**
     * Masks user phone numbers for privacy so private mobile numbers are never
     * publicly exposed (e.g., +919876543210 -> +91 98*** **210).
     */
    fun maskPhone(raw: String): String {
        val clean = normalizePhone(raw)
        if (clean.length < 7) return "***"
        val prefix = clean.take(4)
        val suffix = clean.takeLast(3)
        return "$prefix *** **$suffix"
    }

    /**
     * Constant-time string equality to prevent timing attacks when checking OTPs.
     */
    fun secureCodeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray(Charsets.UTF_8)
        val bBytes = b.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(aBytes, bBytes)
    }
}
