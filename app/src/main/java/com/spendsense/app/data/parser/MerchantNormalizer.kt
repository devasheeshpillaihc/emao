package com.spendsense.app.data.parser

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Normalizes merchant names from raw SMS/notification text.
 * Strips common prefixes, payment gateway names, and standardizes formatting.
 */
@Singleton
class MerchantNormalizer @Inject constructor() {

    companion object {
        // Prefixes to strip from merchant names
        private val STRIP_PREFIXES = listOf(
            "UPI-", "UPI/", "upi-", "upi/",
            "PAYU-", "PAYU*", "PayU-", "PayU*",
            "RAZORPAY*", "Razorpay*", "RAZORPAY-",
            "CASHFREE*", "CASHFREE-",
            "BILLDESK*", "BILLDESK-",
            "AIRPAY*", "AIRPAY-",
            "PHONEPE*", "PHONEPE-",
            "GPAY-", "GOOGLEPAY-",
            "PAYTM*", "PAYTM-",
            "IMT/", "IMPS/", "NEFT/", "RTGS/",
            "POS ", "POS-", "POS/",
            "BIL/", "BILL/",
        )

        // Suffixes to strip
        private val STRIP_SUFFIXES = listOf(
            " UPI", " DEBIT", " CREDIT",
            " NEFT", " RTGS", " IMPS",
            " PVT LTD", " PVT. LTD.", " PVT.LTD",
            " PRIVATE LIMITED", " PRIVATE LTD",
            " LTD", " LIMITED", " LLP",
            " INC", " CORP",
            " INDIA", " IN",
        )

        // Common merchant name mappings (raw → clean)
        private val KNOWN_MERCHANTS = mapOf(
            "swiggy" to "Swiggy",
            "zomato" to "Zomato",
            "uber" to "Uber",
            "ola" to "Ola",
            "rapido" to "Rapido",
            "flipkart" to "Flipkart",
            "amazon" to "Amazon",
            "myntra" to "Myntra",
            "ajio" to "AJIO",
            "meesho" to "Meesho",
            "bigbasket" to "BigBasket",
            "blinkit" to "Blinkit",
            "zepto" to "Zepto",
            "instamart" to "Swiggy Instamart",
            "dunzo" to "Dunzo",
            "jiomart" to "JioMart",
            "dmart" to "DMart",
            "reliance" to "Reliance",
            "netflix" to "Netflix",
            "hotstar" to "Disney+ Hotstar",
            "spotify" to "Spotify",
            "youtube" to "YouTube Premium",
            "google" to "Google",
            "apple" to "Apple",
            "microsoft" to "Microsoft",
            "airtel" to "Airtel",
            "jio" to "Jio",
            "vodafone" to "Vi",
            "vi" to "Vi",
            "bsnl" to "BSNL",
            "irctc" to "IRCTC",
            "makemytrip" to "MakeMyTrip",
            "goibibo" to "Goibibo",
            "cleartrip" to "Cleartrip",
            "ixigo" to "Ixigo",
            "redbus" to "redBus",
            "bookmyshow" to "BookMyShow",
            "pvr" to "PVR INOX",
            "inox" to "PVR INOX",
            "petrol" to "Petrol Pump",
            "hpcl" to "HP Petrol",
            "bpcl" to "BP Petrol",
            "iocl" to "Indian Oil",
            "dominos" to "Domino's",
            "mcdonalds" to "McDonald's",
            "starbucks" to "Starbucks",
            "kfc" to "KFC",
            "subway" to "Subway",
            "pizzahut" to "Pizza Hut",
            "burgerking" to "Burger King",
            "decathlon" to "Decathlon",
            "ikea" to "IKEA",
            "croma" to "Croma",
            "pharmacy" to "Pharmacy",
            "apollo" to "Apollo",
            "medplus" to "MedPlus",
            "phonepe" to "PhonePe",
            "gpay" to "Google Pay",
            "paytm" to "Paytm",
            "electricity" to "Electricity Bill",
            "bescom" to "BESCOM",
            "bwssb" to "BWSSB",
            "water" to "Water Bill",
            "broadband" to "Broadband",
            "actfiber" to "ACT Fibernet",
        )
    }

    /**
     * Normalize a raw merchant name to a clean, standardized form.
     */
    fun normalize(rawMerchant: String): String {
        if (rawMerchant.isBlank()) return ""

        var merchant = rawMerchant.trim()

        // Strip known prefixes
        for (prefix in STRIP_PREFIXES) {
            if (merchant.startsWith(prefix, ignoreCase = true)) {
                merchant = merchant.removePrefix(prefix)
                    .removePrefix(prefix.lowercase())
                    .removePrefix(prefix.uppercase())
                break
            }
        }

        // Strip known suffixes
        for (suffix in STRIP_SUFFIXES) {
            if (merchant.endsWith(suffix, ignoreCase = true)) {
                merchant = merchant.dropLast(suffix.length)
                break
            }
        }

        merchant = merchant.trim()

        // Check known merchant mappings
        val lowerMerchant = merchant.lowercase()
        for ((key, value) in KNOWN_MERCHANTS) {
            if (lowerMerchant.contains(key)) {
                return value
            }
        }

        // Clean up formatting
        merchant = merchant
            .replace(Regex("""\s+"""), " ")           // Multiple spaces
            .replace(Regex("""[_*]+"""), " ")           // Underscores and asterisks
            .replace(Regex("""^[-.]+|[-.]+$"""), "")    // Leading/trailing dashes/dots
            .trim()

        // Title case if all uppercase
        if (merchant == merchant.uppercase() && merchant.length > 3) {
            merchant = merchant.split(" ").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        }

        return merchant
    }

    /**
     * Get clean display name for a merchant.
     * Falls back to normalized name if no known mapping exists.
     */
    fun getDisplayName(rawMerchant: String): String {
        val normalized = normalize(rawMerchant)
        return normalized.ifBlank { rawMerchant }
    }
}
