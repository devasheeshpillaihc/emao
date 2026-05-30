package com.spendsense.app.data.db.dao

import androidx.room.*
import com.spendsense.app.data.db.entity.MerchantMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantMappingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mapping: MerchantMappingEntity): Long

    @Query("SELECT * FROM merchant_mappings WHERE raw_name = :rawName LIMIT 1")
    suspend fun getByRawName(rawName: String): MerchantMappingEntity?

    @Query("""
        SELECT * FROM merchant_mappings 
        WHERE raw_name LIKE '%' || :query || '%' OR normalized_name LIKE '%' || :query || '%'
        ORDER BY frequency DESC
        LIMIT :limit
    """)
    fun searchMerchants(query: String, limit: Int = 10): Flow<List<MerchantMappingEntity>>

    @Query("SELECT * FROM merchant_mappings ORDER BY frequency DESC LIMIT :limit")
    fun getTopMerchants(limit: Int = 20): Flow<List<MerchantMappingEntity>>

    @Query("""
        UPDATE merchant_mappings 
        SET frequency = frequency + 1, updated_at = :now 
        WHERE raw_name = :rawName
    """)
    suspend fun incrementFrequency(rawName: String, now: Long = System.currentTimeMillis())
}
