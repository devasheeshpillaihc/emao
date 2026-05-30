package com.spendsense.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation destinations.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    data object Transactions : Screen(
        route = "transactions",
        title = "Transactions",
        selectedIcon = Icons.Filled.Receipt,
        unselectedIcon = Icons.Outlined.Receipt
    )

    data object Analytics : Screen(
        route = "analytics",
        title = "Analytics",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    )

    data object Budgets : Screen(
        route = "budgets",
        title = "Budgets",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    data object AddTransaction : Screen(
        route = "add_transaction?type={type}",
        title = "Add Transaction"
    ) {
        fun createRoute(type: String = "expense"): String = "add_transaction?type=$type"
    }

    data object TransactionDetail : Screen(
        route = "transaction/{transactionId}",
        title = "Transaction Detail"
    ) {
        fun createRoute(transactionId: Long): String = "transaction/$transactionId"
    }

    companion object {
        val bottomNavItems = listOf(Dashboard, Transactions, Analytics, Budgets, Settings)
    }
}
