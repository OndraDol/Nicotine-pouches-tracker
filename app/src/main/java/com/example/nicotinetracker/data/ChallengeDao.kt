package com.example.nicotinetracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<Challenge>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: Challenge)

    @Query("SELECT * FROM challenges WHERE isCompleted = 0 AND endDate > :currentTime")
    fun getActiveChallenges(currentTime: LocalDateTime): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE isCompleted = 1")
    fun getCompletedChallenges(): Flow<List<Challenge>>

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Query("UPDATE challenges SET currentProgress = currentProgress + :progressAmount WHERE id = :challengeId")
    suspend fun updateChallengeProgress(challengeId: Int, progressAmount: Int)

    @Query("UPDATE challenges SET isCompleted = 1 WHERE id = :challengeId")
    suspend fun markChallengeAsCompleted(challengeId: Int)

    @Query("DELETE FROM challenges WHERE endDate < :currentTime")
    suspend fun deleteExpiredChallenges(currentTime: LocalDateTime)
}
