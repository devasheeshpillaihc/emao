package com.spendsense.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for expense categories.
 * Includes default system categories and user-created custom categories.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val icon: String = "more_horiz",

    val color: Long = 0xFF6C63FF,

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = true,

    val budget: Double? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
