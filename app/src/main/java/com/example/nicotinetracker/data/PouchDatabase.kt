package com.example.nicotinetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nicotinetracker.data.achievement.Achievement
import com.example.nicotinetracker.data.achievement.AchievementDao

@Database(
    entities = [
        Pouch::class,
        Brand::class,
        PouchType::class,
        Achievement::class,
        Challenge::class  // Přidána entita Challenge
    ],
    version = 5,  // Zvýšena verze pro novou migraci
    exportSchema = false
)
@TypeConverters(
    LocalDateTimeConverter::class,
    ChallengeTypeConverter::class,
    ChallengeDifficultyConverter::class
)
abstract class PouchDatabase : RoomDatabase() {
    
    abstract fun pouchDao(): PouchDao
    abstract fun brandDao(): BrandDao
    abstract fun pouchTypeDao(): PouchTypeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun challengeDao(): ChallengeDao  // Přidána metoda pro ChallengeDao
    
    companion object {
        @Volatile
        private var INSTANCE: PouchDatabase? = null
        
        fun getDatabase(context: Context): PouchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PouchDatabase::class.java,
                    "pouch_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Konvertory pro Challenge enum
class ChallengeTypeConverter {
    @TypeConverter
    fun fromChallengeType(type: ChallengeType): String {
        return type.name
    }

    @TypeConverter
    fun toChallengeType(typeName: String): ChallengeType {
        return ChallengeType.valueOf(typeName)
    }
}

class ChallengeDifficultyConverter {
    @TypeConverter
    fun fromChallengeDifficulty(difficulty: ChallengeDifficulty): String {
        return difficulty.name
    }

    @TypeConverter
    fun toChallengeDifficulty(difficultyName: String): ChallengeDifficulty {
        return ChallengeDifficulty.valueOf(difficultyName)
    }
}
