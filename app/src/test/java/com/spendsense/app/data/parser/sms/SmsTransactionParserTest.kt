package com.spendsense.app.data.parser.sms

import com.spendsense.app.domain.model.TransactionDirection
import com.spendsense.app.domain.model.TransactionSource
import com.spendsense.app.domain.model.TransactionStatus
import com.spendsense.app.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SmsTransactionParser.
 * Tests parsing of 15+ Indian bank SMS formats.
 */
class SmsTransactionParserTest {

    private lateinit var parser: SmsTransactionParser

    @Before
    fun setup() {
        parser = SmsTransactionParser()
    }

    // ── SBI SMS Tests ──

    @Test
    fun `parse SBI debit SMS`() {
        val sms = "Your a/c no. XX1234 is debited for Rs.500.00 on 15-01-24 by a]transfer to JOHN DOE. Ref No 234567890123. If not done by u, call 1800112211."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-SBIBNK")

        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals(TransactionStatus.SUCCESS, result.status)
    }

    @Test
    fun `parse SBI credit SMS`() {
        val sms = "Your a/c no. XX5678 is credited by Rs.25,000.00 on 15-01-24 by a]transfer from ACME CORP (Ref No 345678901234)."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-SBIBNK")

        assertNotNull(result)
        assertEquals(25000.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.CREDIT, result.direction)
    }

    // ── HDFC SMS Tests ──

    @Test
    fun `parse HDFC debit card SMS`() {
        val sms = "Rs.1,250.00 debited from a/c **1234 on 20-Jan-24 to VPA merchant@ybl (UPI Ref No 412345678901). Avl Bal Rs.45,678.90."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-HDFCBK")

        assertNotNull(result)
        assertEquals(1250.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
        assertEquals(TransactionType.UPI, result.transactionType)
    }

    @Test
    fun `parse HDFC credit card SMS`() {
        val sms = "Alert: Rs.3,500.00 spent on your HDFC Bank Credit Card ending 9876 at AMAZON on 2024-01-20. Avl limit: Rs.1,50,000."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-HDFCBK")

        assertNotNull(result)
        assertEquals(3500.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    // ── ICICI SMS Tests ──

    @Test
    fun `parse ICICI UPI debit SMS`() {
        val sms = "Dear Customer, Rs. 750.00 has been debited from your Acct XX4567 on 20-01-24 for UPI/DR/412345678901/Swiggy. Your Bal is Rs.12,345.67."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-ICICIB")

        assertNotNull(result)
        assertEquals(750.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    @Test
    fun `parse ICICI credit SMS`() {
        val sms = "Dear Customer, your Acct XX4567 has been credited with Rs.50,000.00 on 15-01-24. Info: NEFT-SALARY. Avl Bal: Rs.1,50,000.00."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-ICICIB")

        assertNotNull(result)
        assertEquals(50000.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.CREDIT, result.direction)
    }

    // ── Axis Bank SMS Tests ──

    @Test
    fun `parse Axis debit SMS`() {
        val sms = "INR 2,500.00 debited from A/c no. XX9012 on 20-Jan-24. Info: UPI/412345678901/Payment to Flipkart. Avl bal: INR 35,000.00."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-AXISBK")

        assertNotNull(result)
        assertEquals(2500.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    // ── Kotak Bank SMS Tests ──

    @Test
    fun `parse Kotak UPI SMS`() {
        val sms = "Sent Rs.400 from Kotak Bank AC X1234 to uber@axisbank on 20-01-24. UPI Ref 412345678901. Not you? Call 18002740110."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-KOTAKB")

        assertNotNull(result)
        assertEquals(400.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.DEBIT, result.direction)
    }

    // ── Failed Transaction Tests ──

    @Test
    fun `parse failed transaction SMS`() {
        val sms = "Dear Customer, your transaction of Rs.1,000.00 to MERCHANT has failed. Ref: 123456789. If debited, amount will be reversed."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-HDFCBK")

        assertNotNull(result)
        assertEquals(TransactionStatus.FAILED, result!!.status)
    }

    // ── Refund Tests ──

    @Test
    fun `parse refund SMS`() {
        val sms = "Refund of Rs.999.00 has been credited to your a/c XX1234. Ref No: REF123456. -SBI"
        val result = parser.parse(sms, TransactionSource.SMS, "VM-SBIBNK")

        assertNotNull(result)
        assertEquals(999.0, result!!.amount, 0.01)
        assertEquals(TransactionDirection.REFUND, result.direction)
    }

    // ── Amount Extraction Tests ──

    @Test
    fun `extract amount with comma formatting`() {
        val amount = SmsBankPatterns.extractAmount("Rs.1,25,000.50 debited from your account")
        assertNotNull(amount)
        assertEquals(125000.50, amount!!, 0.01)
    }

    @Test
    fun `extract amount with rupee symbol`() {
        val amount = SmsBankPatterns.extractAmount("₹500 debited from your account")
        assertNotNull(amount)
        assertEquals(500.0, amount!!, 0.01)
    }

    @Test
    fun `extract amount with INR prefix`() {
        val amount = SmsBankPatterns.extractAmount("INR 3,456.78 transferred to")
        assertNotNull(amount)
        assertEquals(3456.78, amount!!, 0.01)
    }

    // ── Non-Financial SMS Tests ──

    @Test
    fun `return null for non-financial SMS`() {
        val sms = "Your OTP for login is 123456. Valid for 5 minutes."
        val result = parser.parse(sms, TransactionSource.SMS, "VM-SBIBNK")
        assertNull(result)
    }

    @Test
    fun `return null for promotional SMS`() {
        val sms = "Get 20% off on your next purchase! Use code SAVE20. Shop now at example.com"
        val result = parser.parse(sms, TransactionSource.SMS, "BZ-PROMO")
        assertNull(result)
    }

    // ── Transaction Type Detection Tests ──

    @Test
    fun `detect UPI transaction type`() {
        val type = SmsBankPatterns.detectTransactionType("UPI/DR/412345678901/merchant")
        assertEquals(TransactionType.UPI, type)
    }

    @Test
    fun `detect credit card transaction type`() {
        val type = SmsBankPatterns.detectTransactionType("Your Credit Card XX1234 has been charged")
        assertEquals(TransactionType.CREDIT_CARD, type)
    }

    @Test
    fun `detect NEFT transaction type`() {
        val type = SmsBankPatterns.detectTransactionType("NEFT transfer to beneficiary account")
        assertEquals(TransactionType.BANK_TRANSFER, type)
    }

    @Test
    fun `detect ATM withdrawal`() {
        val type = SmsBankPatterns.detectTransactionType("ATM cash withdrawal of Rs.5000")
        assertEquals(TransactionType.ATM, type)
    }

    // ── Reference Number Extraction Tests ──

    @Test
    fun `extract UPI reference number`() {
        val ref = SmsBankPatterns.extractReferenceNumber("UPI Ref No 412345678901")
        assertEquals("412345678901", ref)
    }

    @Test
    fun `extract generic reference number`() {
        val ref = SmsBankPatterns.extractReferenceNumber("Ref No: TXN123456789")
        assertEquals("TXN123456789", ref)
    }

    // ── Bank Sender ID Detection ──

    @Test
    fun `recognize known bank sender`() {
        assertTrue(SmsBankPatterns.isKnownBankSender("VM-SBIBNK"))
        assertTrue(SmsBankPatterns.isKnownBankSender("VD-HDFCBK"))
        assertTrue(SmsBankPatterns.isKnownBankSender("JM-ICICIB"))
    }

    @Test
    fun `reject unknown sender`() {
        assertFalse(SmsBankPatterns.isKnownBankSender("BZ-PROMO"))
        assertFalse(SmsBankPatterns.isKnownBankSender("AM-PIZZA"))
    }

    // ── isTransactionSms Tests ──

    @Test
    fun `identify transaction SMS`() {
        assertTrue(parser.isTransactionSms(
            "Rs.500 debited from your account",
            "VM-SBIBNK"
        ))
    }

    @Test
    fun `reject non-transaction SMS`() {
        assertFalse(parser.isTransactionSms(
            "Your OTP is 123456",
            "VM-SBIBNK"
        ))
    }
}
