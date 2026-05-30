package com.spendsense.app.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spendsense.app.domain.model.*
import com.spendsense.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Spending metric card with animated counter.
 */
@Composable
fun SpendingCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "amount_animation"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = CardShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = CardShape
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "₹${formatAmount(animatedAmount.toDouble())}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Transaction list item card.
 */
@Composable
fun TransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = CardShape,
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CategoryColors.getColor(transaction.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.category),
                    contentDescription = transaction.category.displayName,
                    tint = CategoryColors.getColor(transaction.category),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Merchant & category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.normalizedMerchant.ifBlank {
                        transaction.merchant.ifBlank { transaction.category.displayName }
                    },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryChip(category = transaction.category, small = true)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatDateTime(transaction.dateTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Amount
            val amountColor = when (transaction.direction) {
                TransactionDirection.CREDIT -> Success
                TransactionDirection.REFUND -> Secondary
                TransactionDirection.DEBIT -> MaterialTheme.colorScheme.onSurface
            }
            val prefix = when (transaction.direction) {
                TransactionDirection.CREDIT -> "+"
                TransactionDirection.REFUND -> "+"
                TransactionDirection.DEBIT -> "-"
            }
            Text(
                text = "$prefix₹${formatAmount(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }
    }
}

/**
 * Category chip with color and optional icon.
 */
@Composable
fun CategoryChip(
    category: Category,
    modifier: Modifier = Modifier,
    small: Boolean = false
) {
    val color = CategoryColors.getColor(category)

    Surface(
        modifier = modifier,
        shape = ChipShape,
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = category.displayName,
            modifier = Modifier.padding(
                horizontal = if (small) 6.dp else 10.dp,
                vertical = if (small) 2.dp else 4.dp
            ),
            style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

/**
 * Budget progress bar with animated fill.
 */
@Composable
fun BudgetProgressBar(
    label: String,
    spent: Double,
    budget: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percentage = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )
    val isExceeded = spent > budget
    val barColor = if (isExceeded) Error else color

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "₹${formatAmount(spent)} / ₹${formatAmount(budget)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isExceeded) Error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(ProgressBarShape),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.12f),
        )
        if (isExceeded) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Exceeded by ₹${formatAmount(spent - budget)}",
                style = MaterialTheme.typography.labelSmall,
                color = Error
            )
        }
    }
}

/**
 * Empty state placeholder with icon and message.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (action != null) {
            Spacer(Modifier.height(24.dp))
            action()
        }
    }
}

// ── Utilities ──

fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.FOOD_DINING -> Icons.Filled.Restaurant
        Category.GROCERIES -> Icons.Filled.ShoppingCart
        Category.TRANSPORT -> Icons.Filled.DirectionsCar
        Category.SHOPPING -> Icons.Filled.ShoppingBag
        Category.BILLS_UTILITIES -> Icons.Filled.ReceiptLong
        Category.ENTERTAINMENT -> Icons.Filled.Movie
        Category.HEALTHCARE -> Icons.Filled.LocalHospital
        Category.EDUCATION -> Icons.Filled.School
        Category.TRAVEL -> Icons.Filled.Flight
        Category.SUBSCRIPTIONS -> Icons.Filled.Subscriptions
        Category.INVESTMENTS -> Icons.Filled.TrendingUp
        Category.TRANSFERS -> Icons.Filled.SwapHoriz
        Category.INCOME -> Icons.Filled.AccountBalanceWallet
        Category.SALARY -> Icons.Filled.Payments
        Category.OTHER -> Icons.Filled.MoreHoriz
    }
}

fun formatAmount(amount: Double): String {
    return when {
        amount >= 10_000_000 -> String.format("%.2fCr", amount / 10_000_000)
        amount >= 100_000 -> String.format("%.2fL", amount / 100_000)
        amount >= 1000 -> String.format("%,.0f", amount)
        else -> String.format("%.2f", amount)
    }
}

fun formatDateTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 172800_000 -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

fun formatFullDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
