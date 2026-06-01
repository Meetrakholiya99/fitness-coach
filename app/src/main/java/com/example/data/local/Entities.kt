package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val age: Int,
    val gender: String,
    val height: Double,
    val weight: Double,
    val fitnessLevel: String,
    val goal: String,
    val location: String,
    val equipment: String,
    val daysPerWeek: Int,
    val injuries: String
)

@Entity(tableName = "latest_plan")
data class LatestPlanEntity(
    @PrimaryKey val id: Int = 1,
    val goal: String,
    val dailyCalories: Int,
    val protein: String,
    val carbs: String,
    val fat: String,
    val rawJsonStr: String
)

@Entity(tableName = "progress_log")
data class ProgressLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val weight: Double,
    val bodyFat: Double,
    val bmi: Double,
    val caloriesBurned: Double,
    val note: String = ""
)

@Entity(tableName = "personal_record")
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val date: String
)

@Entity(tableName = "streak_stats")
data class StreakStatsEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val lastActiveDate: String = "" // YYYY-MM-DD
)
