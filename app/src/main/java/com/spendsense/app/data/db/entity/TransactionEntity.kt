package com.spendsense.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for financial transactions.
 * Indexed on fingerprint for fast duplicate detection, and on dateTime for chronological queries.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["fingerprint"], unique = true),
        Index(value = ["date_time"]),
        Index(value = ["category"]),
        Index(value = ["merchant"]),
        Index(value = ["normalized_merchant"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double,

    val currency: String = "INR",

    val merchant: String = "",

    @ColumnInfo(name = "normalized_merchant")
    val normalizedMerchant: String = "",

    val category: String = "OTHER",

    @ColumnInfo(name = "transaction_type")
    val transactionType: String = "UNKNOWN",

    val direction: String = "DEBIT",

    @ColumnInfo(name = "date_time")
    val dateTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "reference_number")
    val referenceNumber: String? = null,

    val source: String = "MANUAL",

    @ColumnInfo(name = "source_raw")
    val sourceRaw: String? = null,

    @ColumnInfo(name = "account_info")
    val accountInfo: String? = null,

    val notes: String? = null,

    val tags: String? = null,

    @ColumnInfo(name = "receipt_uri")
    val receiptUri: String? = null,

    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean = false,

    @ColumnInfo(name = "recurring_id")
    val recurringId: Long? = null,

    val fingerprint: String = "",

    val status: String = "SUCCESS",

    @ColumnInfo(name = "is_edited")
    val isEdited: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
