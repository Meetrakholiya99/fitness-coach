package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    // Latest Plan
    @Query("SELECT * FROM latest_plan WHERE id = 1")
    fun getLatestPlan(): Flow<LatestPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLatestPlan(plan: LatestPlanEntity)

    // Progress Logs
    @Query("SELECT * FROM progress_log ORDER BY date DESC, id DESC")
    fun getAllLogs(): Flow<List<ProgressLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressLog(log: ProgressLogEntity)

    @Query("DELETE FROM progress_log WHERE id = :id")
    suspend fun deleteProgressLog(id: Int)

    // Personal Records
    @Query("SELECT * FROM personal_record ORDER BY weight DESC, reps DESC")
    fun getAllPRs(): Flow<List<PersonalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPR(pr: PersonalRecordEntity)

    @Query("DELETE FROM personal_record WHERE id = :id")
    suspend fun deletePR(id: Int)

    // Streak / stats
    @Query("SELECT * FROM streak_stats WHERE id = 1")
    fun getStreakStats(): Flow<StreakStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreakStats(stats: StreakStatsEntity)
}
