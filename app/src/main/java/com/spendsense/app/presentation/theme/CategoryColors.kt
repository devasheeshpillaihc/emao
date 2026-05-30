package com.spendsense.app.presentation.theme

import androidx.compose.ui.graphics.Color
import com.spendsense.app.domain.model.Category

/**
 * Utility to get colors for each expense category.
 */
object CategoryColors {
    fun getColor(category: Category): Color {
        return when (category) {
            Category.FOOD_DINING -> CategoryFoodColor
            Category.GROCERIES -> CategoryGroceriesColor
            Category.TRANSPORT -> CategoryTransportColor
            Category.SHOPPING -> CategoryShoppingColor
            Category.BILLS_UTILITIES -> CategoryBillsColor
            Category.ENTERTAINMENT -> CategoryEntertainmentColor
            Category.HEALTHCARE -> CategoryHealthcareColor
            Category.EDUCATION -> CategoryEducationColor
            Category.TRAVEL -> CategoryTravelColor
            Category.SUBSCRIPTIONS -> CategorySubscriptionsColor
            Category.INVESTMENTS -> CategoryInvestmentsColor
            Category.TRANSFERS -> CategoryTransfersColor
            Category.INCOME -> CategoryIncomeColor
            Category.SALARY -> CategoryIncomeColor
            Category.OTHER -> CategoryOtherColor
        }
    }

    fun getAllColors(): List<Color> {
        return Category.entries.map { getColor(it) }
    }
}
