package com.spendsense.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for recurring transaction templates.
 */
@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double,

    val merchant: String,

    val category: String = "OTHER",

    val frequency: String = "MONTHLY",

    @ColumnInfo(name = "next_due_date")
    val nextDueDate: Long,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
