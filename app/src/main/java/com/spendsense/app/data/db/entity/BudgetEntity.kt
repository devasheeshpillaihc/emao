package com.spendsense.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for monthly budget limits.
 * When categoryId is null, it represents the overall monthly budget.
 */
@Entity(
    tableName = "budgets",
    indices = [
        androidx.room.Index(value = ["month", "year", "category_id"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,

    @ColumnInfo(name = "category_name")
    val categoryName: String? = null,

    val amount: Double,

    val month: Int,

    val year: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
