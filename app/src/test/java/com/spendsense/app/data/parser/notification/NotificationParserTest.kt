package com.spendsense.app.data.parser.notification

import com.spendsense.app.domain.model.TransactionDirection
import com.spendsense.app.domain.model.TransactionSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NotificationParser.
 * Tests parsing of Google Pay, PhonePe, Paytm, and generic UPI notifications.
 */
class NotificationParserTest {

    private lateinit var parser: NotificationParser

    @Before
    fun setup() {
        parser = NotificationParser()
    }

    // ── Google Pay Tests ──

    @Test
    fun `parse Google Pay paid notification`() {
        val text = "Paid ₹250 to Swiggy"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.google.android.apps.nbu.paisa.user")

        assertNotNull(result)
        assertEquals(250.0, result!!.amount, 0.01)
        assertEquals("Swiggy", result.merchant)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    @Test
    fun `parse Google Pay received notification`() {
        val text = "Received ₹5,000 from John Doe"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.google.android.apps.nbu.paisa.user")

        assertNotNull(result)
        assertEquals(5000.0, result!!.amount, 0.01)
        assertEquals("John Doe", result.merchant)
        assertEquals(TransactionDirection.CREDIT, result.direction)
    }

    @Test
    fun `parse Google Pay with reference`() {
        val text = "Paid ₹1,500.00 to Amazon. UPI Ref 412345678901"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.google.android.apps.nbu.paisa.user")

        assertNotNull(result)
        assertEquals(1500.0, result!!.amount, 0.01)
        assertEquals("412345678901", result.referenceNumber)
    }

    // ── PhonePe Tests ──

    @Test
    fun `parse PhonePe paid notification`() {
        val text = "Paid ₹350 to Uber via PhonePe"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.phonepe.app")

        assertNotNull(result)
        assertEquals(350.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    @Test
    fun `parse PhonePe received notification`() {
        val text = "Received ₹2,000 from Friend Name"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.phonepe.app")

        assertNotNull(result)
        assertEquals(2000.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.CREDIT, result.direction)
    }

    @Test
    fun `parse PhonePe sent successfully`() {
        val text = "Sent ₹800 to Merchant successfully"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.phonepe.app")

        assertNotNull(result)
        assertEquals(800.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    // ── Paytm Tests ──

    @Test
    fun `parse Paytm paid notification`() {
        val text = "Money sent ₹1,200 to Zomato"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "net.one97.paytm")

        assertNotNull(result)
        assertEquals(1200.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    @Test
    fun `parse Paytm received notification`() {
        val text = "Money received ₹500 from Contact Name"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "net.one97.paytm")

        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.CREDIT, result.direction)
    }

    // ── Generic Tests ──

    @Test
    fun `parse generic UPI notification`() {
        val text = "You paid ₹999 at Flipkart"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.unknown.app")

        assertNotNull(result)
        assertEquals(999.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    @Test
    fun `parse failed notification`() {
        val text = "Payment of ₹500 to Merchant failed. Please try again."
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.phonepe.app")

        assertNotNull(result)
        assertEquals(com.spendsense.app.domain.model.TransactionStatus.FAILED, result!!.status)
    }

    @Test
    fun `return null for non-transaction notification`() {
        val text = "Check out the latest offers on PhonePe!"
        val result = parser.parse(text, TransactionSource.NOTIFICATION, "com.phonepe.app")
        assertNull(result)
    }

    // ── Package Monitoring Tests ──

    @Test
    fun `should monitor known packages`() {
        assertTrue(parser.shouldMonitor("com.google.android.apps.nbu.paisa.user"))
        assertTrue(parser.shouldMonitor("com.phonepe.app"))
        assertTrue(parser.shouldMonitor("net.one97.paytm"))
    }

    @Test
    fun `should not monitor unknown packages`() {
        assertFalse(parser.shouldMonitor("com.whatsapp.w4b"))
        assertFalse(parser.shouldMonitor("com.random.app"))
    }
}
