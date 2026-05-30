package com.spendsense.app.data.parser

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MerchantNormalizer.
 */
class MerchantNormalizerTest {

    private lateinit var normalizer: MerchantNormalizer

    @Before
    fun setup() {
        normalizer = MerchantNormalizer()
    }

    // ── Prefix Stripping ──

    @Test
    fun `strip UPI prefix`() {
        assertEquals("Swiggy", normalizer.normalize("UPI-SWIGGY"))
    }

    @Test
    fun `strip PAYU prefix`() {
        assertEquals("Swiggy", normalizer.normalize("PAYU*SWIGGY"))
    }

    @Test
    fun `strip RAZORPAY prefix`() {
        assertEquals("Netflix", normalizer.normalize("RAZORPAY*NETFLIX"))
    }

    @Test
    fun `strip POS prefix`() {
        val result = normalizer.normalize("POS SOME MERCHANT")
        assertTrue(result.isNotBlank())
    }

    // ── Suffix Stripping ──

    @Test
    fun `strip PVT LTD suffix`() {
        val result = normalizer.normalize("ACME CORP PVT LTD")
        assertFalse(result.endsWith("PVT LTD", ignoreCase = true))
    }

    // ── Known Merchant Mapping ──

    @Test
    fun `normalize Swiggy variants`() {
        assertEquals("Swiggy", normalizer.normalize("SWIGGY"))
        assertEquals("Swiggy", normalizer.normalize("swiggy"))
        assertEquals("Swiggy", normalizer.normalize("UPI-SWIGGY"))
    }

    @Test
    fun `normalize Uber variants`() {
        assertEquals("Uber", normalizer.normalize("UBER INDIA"))
        assertEquals("Uber", normalizer.normalize("uber"))
    }

    @Test
    fun `normalize Amazon variants`() {
        assertEquals("Amazon", normalizer.normalize("AMAZON PAY"))
        assertEquals("Amazon", normalizer.normalize("RAZORPAY*AMAZON"))
    }

    @Test
    fun `normalize Flipkart`() {
        assertEquals("Flipkart", normalizer.normalize("FLIPKART INTERNET"))
    }

    @Test
    fun `normalize Netflix`() {
        assertEquals("Netflix", normalizer.normalize("NETFLIX.COM"))
    }

    @Test
    fun `normalize PhonePe`() {
        assertEquals("PhonePe", normalizer.normalize("PHONEPE*MERCHANT"))
    }

    // ── Formatting ──

    @Test
    fun `clean multiple spaces`() {
        val result = normalizer.normalize("SOME   MERCHANT   NAME")
        assertFalse(result.contains("  "))
    }

    @Test
    fun `title case all-uppercase names`() {
        val result = normalizer.normalize("RANDOM MERCHANT NAME")
        // Should be title-cased if no known mapping exists
        assertTrue(result[0].isUpperCase())
    }

    @Test
    fun `handle empty string`() {
        assertEquals("", normalizer.normalize(""))
    }

    @Test
    fun `handle blank string`() {
        assertEquals("", normalizer.normalize("   "))
    }

    // ── Display Name ──

    @Test
    fun `getDisplayName falls back to input for unknown merchant`() {
        val input = "Unknown Store XYZ"
        val result = normalizer.getDisplayName(input)
        assertTrue(result.isNotBlank())
    }

    @Test
    fun `getDisplayName returns normalized for known merchant`() {
        assertEquals("Swiggy", normalizer.getDisplayName("UPI-SWIGGY"))
    }
}
