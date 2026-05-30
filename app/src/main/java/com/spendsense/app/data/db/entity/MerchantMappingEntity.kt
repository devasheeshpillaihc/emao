package com.spendsense.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for merchant name mapping and normalization.
 * Stores raw-to-normalized name mappings and preferred categories.
 */
@Entity(
    tableName = "merchant_mappings",
    indices = [
        Index(value = ["raw_name"], unique = true)
    ]
)
data class MerchantMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "raw_name")
    val rawName: String,

    @ColumnInfo(name = "normalized_name")
    val normalizedName: String,

    val category: String = "OTHER",

    val frequency: Int = 1,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
