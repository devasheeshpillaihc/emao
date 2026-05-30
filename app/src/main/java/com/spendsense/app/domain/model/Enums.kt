package com.spendsense.app.domain.model

/**
 * Expense/Income categories for transactions.
 */
enum class Category(val displayName: String, val icon: String) {
    FOOD_DINING("Food & Dining", "restaurant"),
    GROCERIES("Groceries", "shopping_cart"),
    TRANSPORT("Transport", "directions_car"),
    SHOPPING("Shopping", "shopping_bag"),
    BILLS_UTILITIES("Bills & Utilities", "receipt_long"),
    ENTERTAINMENT("Entertainment", "movie"),
    HEALTHCARE("Healthcare", "local_hospital"),
    EDUCATION("Education", "school"),
    TRAVEL("Travel", "flight"),
    SUBSCRIPTIONS("Subscriptions", "subscriptions"),
    INVESTMENTS("Investments", "trending_up"),
    TRANSFERS("Transfers", "swap_horiz"),
    INCOME("Income", "account_balance_wallet"),
    SALARY("Salary", "payments"),
    OTHER("Other", "more_horiz");

    companion object {
        fun fromDisplayName(name: String): Category {
            return entries.find {
                it.displayName.equals(name, ignoreCase = true)
            } ?: OTHER
        }
    }
}

/**
 * Transaction payment method types.
 */
enum class TransactionType(val displayName: String) {
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    ATM("ATM"),
    CASH("Cash"),
    WALLET("Wallet"),
    UNKNOWN("Unknown");

    companion object {
        fun fromDisplayName(name: String): TransactionType {
            return entries.find {
                it.displayName.equals(name, ignoreCase = true)
            } ?: UNKNOWN
        }
    }
}

/**
 * Direction of money flow.
 */
enum class TransactionDirection {
    DEBIT,
    CREDIT,
    REFUND;
}

/**
 * Status of a transaction.
 */
enum class TransactionStatus {
    SUCCESS,
    FAILED,
    PENDING;
}

/**
 * Source from which the transaction was detected.
 */
enum class TransactionSource {
    SMS,
    NOTIFICATION,
    EMAIL,
    MANUAL;
}

/**
 * Frequency for recurring transactions.
 */
enum class RecurringFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");
}
