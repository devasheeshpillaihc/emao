package com.spendsense.app.data.parser

import com.spendsense.app.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Predicts expense category based on merchant name and transaction context.
 * Uses a keyword-based approach with 200+ pre-seeded merchant-to-category mappings.
 */
@Singleton
class CategoryPredictor @Inject constructor() {

    companion object {
        /**
         * Keyword to category mapping.
         * Order matters — more specific keywords should come first.
         */
        private val KEYWORD_MAPPINGS: List<Pair<List<String>, Category>> = listOf(
            // Food & Dining
            listOf(
                "swiggy", "zomato", "dominos", "domino", "mcdonalds", "mcdonald",
                "starbucks", "kfc", "subway", "pizzahut", "pizza hut", "burger king",
                "burgerking", "dunkin", "cafe", "restaurant", "food", "biryani",
                "dine", "dining", "eatery", "kitchen", "dhaba", "canteen", "mess",
                "bakery", "haldiram", "barbeque", "bbq", "chaayos", "chai",
                "freshmen", "faasos", "behrouz", "box8", "eatsure", "rebel foods"
            ) to Category.FOOD_DINING,

            // Groceries
            listOf(
                "bigbasket", "blinkit", "zepto", "instamart", "dunzo", "jiomart",
                "dmart", "grofers", "nature basket", "more supermarket", "spencer",
                "reliance fresh", "star bazaar", "grocery", "supermarket", "kirana",
                "vegetables", "fruits", "provision", "dairy", "milk"
            ) to Category.GROCERIES,

            // Transport
            listOf(
                "uber", "ola", "rapido", "metro", "irctc", "redbus",
                "petrol", "diesel", "fuel", "hpcl", "bpcl", "iocl",
                "indian oil", "shell", "parking", "toll", "fastag",
                "aarafat", "cab", "taxi", "auto", "rickshaw", "bus",
                "namma yatri"
            ) to Category.TRANSPORT,

            // Shopping
            listOf(
                "flipkart", "amazon", "myntra", "ajio", "meesho", "snapdeal",
                "nykaa", "tatacliq", "croma", "reliance digital", "vijay sales",
                "decathlon", "ikea", "h&m", "zara", "westside", "pantaloons",
                "shoppers stop", "lifestyle", "max fashion", "clothing", "shoes",
                "electronics", "mobile", "laptop"
            ) to Category.SHOPPING,

            // Bills & Utilities
            listOf(
                "electricity", "bescom", "tata power", "adani electricity",
                "water", "bwssb", "gas", "piped gas", "mahanagar gas",
                "broadband", "wifi", "internet", "act fibernet", "airtel fiber",
                "jio fiber", "bsnl", "airtel", "jio", "vodafone", "vi",
                "postpaid", "prepaid", "recharge", "dth", "tata sky",
                "maintenance", "society", "rent", "emi", "loan",
                "insurance", "lic", "premium"
            ) to Category.BILLS_UTILITIES,

            // Entertainment
            listOf(
                "netflix", "hotstar", "disney", "prime video", "spotify",
                "youtube", "gaana", "jiosaavn", "apple music",
                "bookmyshow", "pvr", "inox", "cinema", "movie", "theatre",
                "game", "gaming", "playstation", "xbox", "steam", "epic games"
            ) to Category.ENTERTAINMENT,

            // Healthcare
            listOf(
                "pharmacy", "medical", "hospital", "clinic", "doctor",
                "apollo", "medplus", "netmeds", "1mg", "pharmeasy",
                "diagnostic", "lab", "pathology", "health", "dental",
                "optician", "eye", "ayurveda", "consultation"
            ) to Category.HEALTHCARE,

            // Education
            listOf(
                "school", "college", "university", "tuition", "coaching",
                "udemy", "coursera", "byju", "unacademy", "vedantu",
                "book", "stationery", "library", "education", "exam",
                "skill", "training", "course", "certification"
            ) to Category.EDUCATION,

            // Travel
            listOf(
                "makemytrip", "goibibo", "cleartrip", "ixigo", "yatra",
                "trivago", "oyo", "airbnb", "hotel", "resort", "booking.com",
                "flight", "airline", "indigo", "spicejet", "vistara",
                "air india", "akasa", "travel", "tourism", "passport"
            ) to Category.TRAVEL,

            // Subscriptions
            listOf(
                "subscription", "premium", "pro plan", "annual plan",
                "monthly plan", "membership", "renewal", "auto-debit",
                "recurring", "amazon prime", "zee5", "sonyliv"
            ) to Category.SUBSCRIPTIONS,

            // Investments
            listOf(
                "mutual fund", "sip", "zerodha", "groww", "upstox", "kite",
                "angel broking", "icicidirect", "hdfc securities",
                "stock", "share", "nse", "bse", "investment",
                "fd", "fixed deposit", "rd", "ppf", "nps",
                "gold", "digital gold", "crypto", "bitcoin"
            ) to Category.INVESTMENTS,

            // Transfers
            listOf(
                "transfer", "neft", "rtgs", "imps", "upi transfer",
                "self transfer", "fund transfer", "sent to", "received from"
            ) to Category.TRANSFERS,

            // Income / Salary
            listOf(
                "salary", "credited by employer", "payroll"
            ) to Category.SALARY
        )
    }

    /**
     * Predict category for a transaction based on merchant name.
     *
     * @param merchant The merchant name (raw or normalized)
     * @param rawText Optional raw SMS/notification text for additional context
     * @return Predicted category, defaults to OTHER if no match
     */
    fun predict(merchant: String, rawText: String = ""): Category {
        val searchText = "$merchant $rawText".lowercase()

        for ((keywords, category) in KEYWORD_MAPPINGS) {
            if (keywords.any { keyword -> searchText.contains(keyword) }) {
                return category
            }
        }

        return Category.OTHER
    }

    /**
     * Get top N category suggestions with confidence scores.
     */
    fun getSuggestions(merchant: String, rawText: String = "", topN: Int = 3): List<Pair<Category, Float>> {
        val searchText = "$merchant $rawText".lowercase()
        val scores = mutableMapOf<Category, Int>()

        for ((keywords, category) in KEYWORD_MAPPINGS) {
            val matchCount = keywords.count { keyword -> searchText.contains(keyword) }
            if (matchCount > 0) {
                scores[category] = (scores[category] ?: 0) + matchCount
            }
        }

        if (scores.isEmpty()) {
            return listOf(Category.OTHER to 1.0f)
        }

        val totalScore = scores.values.sum().toFloat()
        return scores.entries
            .sortedByDescending { it.value }
            .take(topN)
            .map { it.key to (it.value / totalScore) }
    }
}
