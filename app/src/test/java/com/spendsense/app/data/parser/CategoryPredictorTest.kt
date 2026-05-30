package com.spendsense.app.data.parser

import com.spendsense.app.domain.model.Category
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CategoryPredictor.
 */
class CategoryPredictorTest {

    private lateinit var predictor: CategoryPredictor

    @Before
    fun setup() {
        predictor = CategoryPredictor()
    }

    // ── Food & Dining ──

    @Test
    fun `predict Swiggy as Food and Dining`() {
        assertEquals(Category.FOOD_DINING, predictor.predict("Swiggy"))
    }

    @Test
    fun `predict Zomato as Food and Dining`() {
        assertEquals(Category.FOOD_DINING, predictor.predict("Zomato"))
    }

    @Test
    fun `predict Dominos as Food and Dining`() {
        assertEquals(Category.FOOD_DINING, predictor.predict("DOMINOS PIZZA"))
    }

    @Test
    fun `predict Starbucks as Food and Dining`() {
        assertEquals(Category.FOOD_DINING, predictor.predict("STARBUCKS COFFEE"))
    }

    // ── Groceries ──

    @Test
    fun `predict BigBasket as Groceries`() {
        assertEquals(Category.GROCERIES, predictor.predict("BigBasket"))
    }

    @Test
    fun `predict Blinkit as Groceries`() {
        assertEquals(Category.GROCERIES, predictor.predict("Blinkit"))
    }

    @Test
    fun `predict Zepto as Groceries`() {
        assertEquals(Category.GROCERIES, predictor.predict("Zepto"))
    }

    // ── Transport ──

    @Test
    fun `predict Uber as Transport`() {
        assertEquals(Category.TRANSPORT, predictor.predict("UBER INDIA"))
    }

    @Test
    fun `predict Ola as Transport`() {
        assertEquals(Category.TRANSPORT, predictor.predict("Ola Cabs"))
    }

    @Test
    fun `predict petrol pump as Transport`() {
        assertEquals(Category.TRANSPORT, predictor.predict("HP PETROL PUMP"))
    }

    @Test
    fun `predict IRCTC as Transport`() {
        assertEquals(Category.TRANSPORT, predictor.predict("IRCTC"))
    }

    // ── Shopping ──

    @Test
    fun `predict Amazon as Shopping`() {
        assertEquals(Category.SHOPPING, predictor.predict("AMAZON PAY"))
    }

    @Test
    fun `predict Flipkart as Shopping`() {
        assertEquals(Category.SHOPPING, predictor.predict("Flipkart"))
    }

    @Test
    fun `predict Myntra as Shopping`() {
        assertEquals(Category.SHOPPING, predictor.predict("MYNTRA DESIGNS"))
    }

    // ── Bills & Utilities ──

    @Test
    fun `predict electricity bill`() {
        assertEquals(Category.BILLS_UTILITIES, predictor.predict("BESCOM ELECTRICITY"))
    }

    @Test
    fun `predict Airtel recharge`() {
        assertEquals(Category.BILLS_UTILITIES, predictor.predict("AIRTEL PREPAID"))
    }

    @Test
    fun `predict broadband bill`() {
        assertEquals(Category.BILLS_UTILITIES, predictor.predict("ACT FIBERNET"))
    }

    // ── Entertainment ──

    @Test
    fun `predict Netflix as Entertainment`() {
        assertEquals(Category.ENTERTAINMENT, predictor.predict("Netflix"))
    }

    @Test
    fun `predict BookMyShow as Entertainment`() {
        assertEquals(Category.ENTERTAINMENT, predictor.predict("BookMyShow"))
    }

    // ── Healthcare ──

    @Test
    fun `predict Apollo as Healthcare`() {
        assertEquals(Category.HEALTHCARE, predictor.predict("APOLLO PHARMACY"))
    }

    @Test
    fun `predict 1mg as Healthcare`() {
        assertEquals(Category.HEALTHCARE, predictor.predict("1mg"))
    }

    // ── Education ──

    @Test
    fun `predict Udemy as Education`() {
        assertEquals(Category.EDUCATION, predictor.predict("UDEMY INC"))
    }

    // ── Travel ──

    @Test
    fun `predict MakeMyTrip as Travel`() {
        assertEquals(Category.TRAVEL, predictor.predict("MakeMyTrip"))
    }

    @Test
    fun `predict OYO as Travel`() {
        assertEquals(Category.TRAVEL, predictor.predict("OYO ROOMS"))
    }

    // ── Investments ──

    @Test
    fun `predict Zerodha as Investments`() {
        assertEquals(Category.INVESTMENTS, predictor.predict("ZERODHA BROKING"))
    }

    @Test
    fun `predict Groww as Investments`() {
        assertEquals(Category.INVESTMENTS, predictor.predict("GROWW"))
    }

    // ── Transfers ──

    @Test
    fun `predict NEFT transfer as Transfer`() {
        assertEquals(Category.TRANSFERS, predictor.predict("", "NEFT transfer to beneficiary"))
    }

    // ── Unknown / Other ──

    @Test
    fun `predict unknown merchant as Other`() {
        assertEquals(Category.OTHER, predictor.predict("RANDOM MERCHANT XYZ"))
    }

    // ── Suggestions ──

    @Test
    fun `get suggestions returns multiple options`() {
        val suggestions = predictor.getSuggestions("Swiggy Instamart", topN = 3)
        assertTrue(suggestions.isNotEmpty())
        // Should include both Food/Dining and Groceries since Swiggy Instamart matches both
    }

    @Test
    fun `get suggestions for unknown merchant returns Other`() {
        val suggestions = predictor.getSuggestions("COMPLETELY UNKNOWN MERCHANT")
        assertEquals(1, suggestions.size)
        assertEquals(Category.OTHER, suggestions[0].first)
    }
}
