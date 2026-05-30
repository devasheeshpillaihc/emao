package com.spendsense.app.data.db.dao

import androidx.room.*
import com.spendsense.app.data.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query("""
        SELECT * FROM budgets 
        WHERE month = :month AND year = :year AND category_id IS NULL 
        LIMIT 1
    """)
    fun getOverallBudget(month: Int, year: Int): Flow<BudgetEntity?>

    @Query("""
        SELECT * FROM budgets 
        WHERE month = :month AND year = :year AND category_name = :categoryName 
        LIMIT 1
    """)
    fun getCategoryBudget(categoryName: String, month: Int, year: Int): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
