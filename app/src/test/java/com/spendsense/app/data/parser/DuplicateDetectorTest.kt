package com.spendsense.app.data.parser

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DuplicateDetector.
 */
class DuplicateDetectorTest {

    private lateinit var detector: DuplicateDetector

    @Before
    fun setup() {
        detector = DuplicateDetector()
    }

    @Test
    fun `same reference number generates same fingerprint`() {
        val fp1 = detector.generateFingerprint(500.0, "Swiggy", 1705987200000L, "REF123456")
        val fp2 = detector.generateFingerprint(500.0, "Swiggy", 1705987300000L, "REF123456")

        assertEquals(fp1, fp2)
    }

    @Test
    fun `different reference numbers generate different fingerprints`() {
        val fp1 = detector.generateFingerprint(500.0, "Swiggy", 1705987200000L, "REF123456")
        val fp2 = detector.generateFingerprint(500.0, "Swiggy", 1705987200000L, "REF789012")

        assertNotEquals(fp1, fp2)
    }

    @Test
    fun `same amount and merchant within time window generates same fingerprint`() {
        val time1 = 1705987200000L // Some fixed time
        val time2 = time1 + 2 * 60 * 1000 // +2 minutes (within 5-min window)

        val fp1 = detector.generateFingerprint(500.0, "Swiggy", time1, null)
        val fp2 = detector.generateFingerprint(500.0, "Swiggy", time2, null)

        assertEquals(fp1, fp2)
    }

    @Test
    fun `same amount and merchant outside time window generates different fingerprint`() {
        val time1 = 1705987200000L
        val time2 = time1 + 10 * 60 * 1000 // +10 minutes (outside 5-min window)

        val fp1 = detector.generateFingerprint(500.0, "Swiggy", time1, null)
        val fp2 = detector.generateFingerprint(500.0, "Swiggy", time2, null)

        assertNotEquals(fp1, fp2)
    }

    @Test
    fun `different amounts generate different fingerprints`() {
        val time = 1705987200000L
        val fp1 = detector.generateFingerprint(500.0, "Swiggy", time, null)
        val fp2 = detector.generateFingerprint(600.0, "Swiggy", time, null)

        assertNotEquals(fp1, fp2)
    }

    @Test
    fun `different merchants generate different fingerprints`() {
        val time = 1705987200000L
        val fp1 = detector.generateFingerprint(500.0, "Swiggy", time, null)
        val fp2 = detector.generateFingerprint(500.0, "Zomato", time, null)

        assertNotEquals(fp1, fp2)
    }

    @Test
    fun `potential duplicates same amount within window`() {
        val time = 1705987200000L
        assertTrue(
            detector.arePotentialDuplicates(
                500.0, "Swiggy", time,
                500.0, "Swiggy", time + 60_000
            )
        )
    }

    @Test
    fun `not duplicates different amounts`() {
        val time = 1705987200000L
        assertFalse(
            detector.arePotentialDuplicates(
                500.0, "Swiggy", time,
                600.0, "Swiggy", time + 60_000
            )
        )
    }

    @Test
    fun `not duplicates outside time window`() {
        val time = 1705987200000L
        assertFalse(
            detector.arePotentialDuplicates(
                500.0, "Swiggy", time,
                500.0, "Swiggy", time + 600_000 // 10 minutes
            )
        )
    }

    @Test
    fun `potential duplicates with one blank merchant`() {
        val time = 1705987200000L
        assertTrue(
            detector.arePotentialDuplicates(
                500.0, "Swiggy", time,
                500.0, "", time + 60_000
            )
        )
    }

    @Test
    fun `fingerprint is consistent and deterministic`() {
        val fp1 = detector.generateFingerprint(1234.56, "Test Merchant", 1705987200000L, null)
        val fp2 = detector.generateFingerprint(1234.56, "Test Merchant", 1705987200000L, null)
        assertEquals(fp1, fp2)
        assertTrue(fp1.length == 64) // SHA-256 produces 64-char hex string
    }
}
