package com.example.nicotinetracker.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.utils.AdManager
import com.example.nicotinetracker.utils.LocaleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePouchDatabase(@ApplicationContext context: Context): PouchDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PouchDatabase::class.java,
            "pouch_tracker_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideAdManager(@ApplicationContext context: Context): AdManager {
        return AdManager(context)
    }

    @Provides
    @Singleton
    fun provideLocaleManager(@ApplicationContext context: Context): LocaleManager {
        return LocaleManager(context)
    }

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // DAO Providers
    @Provides
    fun providePouchDao(database: PouchDatabase) = database.pouchDao()

    @Provides
    fun provideBrandDao(database: PouchDatabase) = database.brandDao()

    @Provides
    fun provideChallengeDao(database: PouchDatabase) = database.challengeDao()
}
