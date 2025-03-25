package com.example.nicotinetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class ChallengeType {
    SHORT_TERM,   // Týdenní/měsíční výzvy
    LONG_TERM     // Dlouhodobé výzvy
}

enum class ChallengeDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXTREME
}

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val type: ChallengeType,
    val difficulty: ChallengeDifficulty,
    val targetValue: Int,
    val rewardPoints: Int,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val isCompleted: Boolean = false,
    val currentProgress: Int = 0
)

// Kategorie výzev
sealed class ChallengeCategory {
    object Reduction : ChallengeCategory()
    object Consistency : ChallengeCategory()
    object Health : ChallengeCategory()
    object Milestone : ChallengeCategory()
}

// Tovární metoda pro generování výzev
object ChallengeFactory {
    fun generateShortTermChallenges(): List<Challenge> = listOf(
        // Týdenní výzvy
        Challenge(
            title = "One Week Limit Control",
            description = "Stay within daily limit for 7 consecutive days",
            type = ChallengeType.SHORT_TERM,
            difficulty = ChallengeDifficulty.MEDIUM,
            targetValue = 7,
            rewardPoints = 50,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(7)
        ),
        Challenge(
            title = "Gradual Reduction Week",
            description = "Reduce daily pouch consumption by 2 pouches this week",
            type = ChallengeType.SHORT_TERM,
            difficulty = ChallengeDifficulty.HARD,
            targetValue = 14,  // 2 pouchy méně denně
            rewardPoints = 75,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(7)
        ),
        Challenge(
            title = "Weekend Warrior",
            description = "No pouches during weekend days",
            type = ChallengeType.SHORT_TERM,
            difficulty = ChallengeDifficulty.EXTREME,
            targetValue = 2,
            rewardPoints = 100,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusDays(2)
        )
    )

    fun generateLongTermChallenges(): List<Challenge> = listOf(
        // Dlouhodobé výzvy
        Challenge(
            title = "Monthly Reduction Master",
            description = "Reduce monthly pouch consumption by 25%",
            type = ChallengeType.LONG_TERM,
            difficulty = ChallengeDifficulty.HARD,
            targetValue = 30,  // 25% redukce
            rewardPoints = 250,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusMonths(1)
        ),
        Challenge(
            title = "Consistent Tracker",
            description = "Track pouches every single day for 3 months",
            type = ChallengeType.LONG_TERM,
            difficulty = ChallengeDifficulty.EXTREME,
            targetValue = 90,
            rewardPoints = 500,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusMonths(3)
        ),
        Challenge(
            title = "Health Conscious Journey",
            description = "Keep total nicotine intake below 500mg per month",
            type = ChallengeType.LONG_TERM,
            difficulty = ChallengeDifficulty.MEDIUM,
            targetValue = 500,
            rewardPoints = 150,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusMonths(1)
        )
    )
}
