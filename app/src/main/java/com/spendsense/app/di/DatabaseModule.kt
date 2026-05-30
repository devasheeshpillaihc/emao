package com.spendsense.app.di

import android.content.Context
import androidx.room.Room
import com.spendsense.app.data.db.AppDatabase
import com.spendsense.app.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Hilt module providing the Room database and all DAOs.
 * Uses SQLCipher for at-rest encryption.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        // SQLCipher encryption passphrase
        // In production, generate this from Android Keystore
        val passphrase = SQLiteDatabase.getBytes("SpendSense_Secure_DB_Key_2024".toCharArray())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    @Singleton
    fun provideRecurringTransactionDao(database: AppDatabase): RecurringTransactionDao {
        return database.recurringTransactionDao()
    }

    @Provides
    @Singleton
    fun provideMerchantMappingDao(database: AppDatabase): MerchantMappingDao {
        return database.merchantMappingDao()
    }
}
