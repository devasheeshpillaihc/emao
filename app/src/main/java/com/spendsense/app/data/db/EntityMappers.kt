package com.spendsense.app.data.db

import com.spendsense.app.data.db.dao.CategorySpendingTuple
import com.spendsense.app.data.db.dao.MerchantSpendingTuple
import com.spendsense.app.data.db.dao.MonthlyTrendTuple
import com.spendsense.app.data.db.entity.*
import com.spendsense.app.domain.model.*

/**
 * Extension functions to map between Room entities and domain models.
 */

// ── Transaction Mappers ──

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        currency = currency,
        merchant = merchant,
        normalizedMerchant = normalizedMerchant,
        category = try { Category.valueOf(category) } catch (e: Exception) { Category.OTHER },
        transactionType = try { TransactionType.valueOf(transactionType) } catch (e: Exception) { TransactionType.UNKNOWN },
        direction = try { TransactionDirection.valueOf(direction) } catch (e: Exception) { TransactionDirection.DEBIT },
        dateTime = dateTime,
        referenceNumber = referenceNumber,
        source = try { TransactionSource.valueOf(source) } catch (e: Exception) { TransactionSource.MANUAL },
        sourceRaw = sourceRaw,
        accountInfo = accountInfo,
        notes = notes,
        tags = tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        receiptUri = receiptUri,
        isRecurring = isRecurring,
        recurringId = recurringId,
        fingerprint = fingerprint,
        status = try { TransactionStatus.valueOf(status) } catch (e: Exception) { TransactionStatus.SUCCESS },
        isEdited = isEdited,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        currency = currency,
        merchant = merchant,
        normalizedMerchant = normalizedMerchant,
        category = category.name,
        transactionType = transactionType.name,
        direction = direction.name,
        dateTime = dateTime,
        referenceNumber = referenceNumber,
        source = source.name,
        sourceRaw = sourceRaw,
        accountInfo = accountInfo,
        notes = notes,
        tags = tags.joinToString(","),
        receiptUri = receiptUri,
        isRecurring = isRecurring,
        recurringId = recurringId,
        fingerprint = fingerprint,
        status = status.name,
        isEdited = isEdited,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ── Budget Mappers ──

fun BudgetEntity.toDomain(spent: Double = 0.0): Budget {
    return Budget(
        id = id,
        categoryId = categoryId,
        category = categoryName?.let { name ->
            try { Category.valueOf(name) } catch (e: Exception) { null }
        },
        amount = amount,
        spent = spent,
        month = month,
        year = year,
        createdAt = createdAt
    )
}

fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        categoryId = categoryId,
        categoryName = category?.name,
        amount = amount,
        month = month,
        year = year,
        createdAt = createdAt
    )
}

// ── RecurringTransaction Mappers ──

fun RecurringTransactionEntity.toDomain(): RecurringTransaction {
    return RecurringTransaction(
        id = id,
        amount = amount,
        merchant = merchant,
        category = try { Category.valueOf(category) } catch (e: Exception) { Category.OTHER },
        frequency = try { RecurringFrequency.valueOf(frequency) } catch (e: Exception) { RecurringFrequency.MONTHLY },
        nextDueDate = nextDueDate,
        isActive = isActive,
        notes = notes
    )
}

fun RecurringTransaction.toEntity(): RecurringTransactionEntity {
    return RecurringTransactionEntity(
        id = id,
        amount = amount,
        merchant = merchant,
        category = category.name,
        frequency = frequency.name,
        nextDueDate = nextDueDate,
        isActive = isActive,
        notes = notes
    )
}

// ── Aggregation Tuple Mappers ──

fun CategorySpendingTuple.toDomain(totalSpending: Double): CategorySpending {
    val cat = try { Category.valueOf(category) } catch (e: Exception) { Category.OTHER }
    return CategorySpending(
        category = cat,
        totalAmount = total,
        transactionCount = count,
        percentage = if (totalSpending > 0) (total / totalSpending * 100).toFloat() else 0f
    )
}

fun MerchantSpendingTuple.toDomain(): MerchantSpending {
    return MerchantSpending(
        merchant = merchant,
        totalAmount = total,
        transactionCount = count,
        category = try { Category.valueOf(category) } catch (e: Exception) { Category.OTHER }
    )
}

fun MonthlyTrendTuple.toDomain(): MonthlyTrend {
    val monthNames = arrayOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return MonthlyTrend(
        month = month,
        year = year,
        totalSpending = totalSpending,
        totalIncome = totalIncome,
        label = "${monthNames.getOrElse(month) { "" }} $year"
    )
}

// ── ParsedTransaction to Domain ──

fun ParsedTransaction.toDomain(): Transaction {
    return Transaction(
        amount = amount,
        currency = currency,
        merchant = merchant,
        dateTime = dateTime,
        transactionType = transactionType,
        direction = direction,
        referenceNumber = referenceNumber,
        accountInfo = accountInfo,
        status = status,
        sourceRaw = sourceRaw,
        source = source
    )
}
