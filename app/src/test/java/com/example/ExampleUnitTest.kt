package com.example

import com.example.data.SecurityUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPasswordHashingIsSha256() {
        val raw = "FGTime@123"
        val hash = SecurityUtils.hashPassword(raw)
        assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 produces 64-char hex string
        assertNotEquals(raw, hash)
        assertEquals(hash, SecurityUtils.hashPassword(raw))
    }

    @Test
    fun testOtpGenerationIsSixDigits() {
        val otp = SecurityUtils.generateOtp()
        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })
    }

    @Test
    fun testPhoneNormalizationAndValidation() {
        val raw = "+91 (987) 654-3210"
        val normalized = SecurityUtils.normalizePhone(raw)
        assertEquals("+919876543210", normalized)
        assertTrue(SecurityUtils.isValidPhone(normalized))
        assertFalse(SecurityUtils.isValidPhone("123"))
    }
}
