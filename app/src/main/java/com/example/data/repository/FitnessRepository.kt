package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.network.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FitnessRepository(private val fitnessDao: FitnessDao) {

    private val moshi: Moshi = RetrofitClient.moshiInstance
    private val planAdapter = moshi.adapter(GeneratedPlan::class.java)

    // Flows from database mapped to domain objects
    val userProfile: Flow<UserProfile?> = fitnessDao.getUserProfile().map { entity ->
        entity?.let {
            UserProfile(
                age = it.age,
                gender = it.gender,
                height = it.height,
                weight = it.weight,
                fitnessLevel = it.fitnessLevel,
                goal = it.goal,
                location = it.location,
                equipment = it.equipment,
                daysPerWeek = it.daysPerWeek,
                injuries = it.injuries
            )
        }
    }

    val latestPlan: Flow<GeneratedPlan?> = fitnessDao.getLatestPlan().map { entity ->
        entity?.let {
            try {
                planAdapter.fromJson(it.rawJsonStr)
            } catch (e: Exception) {
                Log.e("FitnessRepository", "Error deserializing plan: ${e.message}")
                null
            }
        }
    }

    val progressLogs: Flow<List<ProgressLogEntity>> = fitnessDao.getAllLogs()
    val personalRecords: Flow<List<PersonalRecordEntity>> = fitnessDao.getAllPRs()
    val streakStats: Flow<StreakStatsEntity?> = fitnessDao.getStreakStats()

    // Save profile to database
    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val entity = UserProfileEntity(
            age = profile.age,
            gender = profile.gender,
            height = profile.height,
            weight = profile.weight,
            fitnessLevel = profile.fitnessLevel,
            goal = profile.goal,
            location = profile.location,
            equipment = profile.equipment,
            daysPerWeek = profile.daysPerWeek,
            injuries = profile.injuries
        )
        fitnessDao.insertUserProfile(entity)
    }

    // Save progress log (Weight change, BMI, body fat %, calories burned)
    suspend fun logProgress(
        weight: Double,
        bodyFat: Double,
        bmi: Double,
        caloriesBurned: Double,
        note: String
    ) = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val log = ProgressLogEntity(
            date = todayStr,
            weight = weight,
            bodyFat = bodyFat,
            bmi = bmi,
            caloriesBurned = caloriesBurned,
            note = note
        )
        fitnessDao.insertProgressLog(log)
    }

    suspend fun deleteLog(id: Int) = withContext(Dispatchers.IO) {
        fitnessDao.deleteProgressLog(id)
    }

    // Save Personal Record
    suspend fun logPR(exerciseName: String, weight: Double, reps: Int) = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val pr = PersonalRecordEntity(
            exerciseName = exerciseName,
            weight = weight,
            reps = reps,
            date = todayStr
        )
        fitnessDao.insertPR(pr)
    }

    suspend fun deletePR(id: Int) = withContext(Dispatchers.IO) {
        fitnessDao.deletePR(id)
    }

    // Complete a workout - Increments streak & saves calories burned
    suspend fun completeWorkout(caloriesBurned: Double) = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Log calories burned
        val defaultWeight = 70.0
        val defaultBmi = 22.0
        val defaultBodyFat = 15.0
        
        // Let's check for latest log to get actual weight, BMI and body fat
        // Suspend fetch or flow first
        val log = ProgressLogEntity(
            date = todayStr,
            weight = 0.0, // Mark as 0 to indicate workout completion log
            bodyFat = 0.0,
            bmi = 0.0,
            caloriesBurned = caloriesBurned,
            note = "Workout completed!"
        )
        fitnessDao.insertProgressLog(log)

        // Increment streak
        fitnessDao.getStreakStats().collect { streamEntry ->
            val cur = streamEntry ?: StreakStatsEntity()
            if (cur.lastActiveDate != todayStr) {
                // Determine if streak is continuous
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var newStreak = 1
                try {
                    val lastDate = dateFormat.parse(cur.lastActiveDate)
                    val todayDate = dateFormat.parse(todayStr)
                    val diff = todayDate.time - lastDate.time
                    val diffDays = diff / (1000 * 60 * 60 * 24)
                    if (diffDays == 1L) {
                        newStreak = cur.currentStreak + 1
                    } else if (diffDays <= 0) {
                        newStreak = cur.currentStreak // same day, do nothing
                    }
                } catch (e: Exception) {
                    newStreak = 1
                }
                
                fitnessDao.insertStreakStats(
                    StreakStatsEntity(
                        currentStreak = newStreak,
                        lastActiveDate = todayStr
                    )
                )
            }
        }
    }

    // Call Gemini API to generate plan
    suspend fun generateWorkoutAndNutritionPlan(profile: UserProfile): Result<GeneratedPlan> = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Please add GEMINI_API_KEY to your Secrets panel."))
        }

        val prompt = """
            Act as an elite certified fitness coach and personal trainer.
            Generate a scientifically accurate, highly detailed, safe, and custom workout and nutrition plan.
            
            User Profile:
            - Age: ${profile.age} years
            - Gender: ${profile.gender}
            - Height: ${profile.height} cm
            - Weight: ${profile.weight} kg
            - Fitness Level: ${profile.fitnessLevel}
            - Goal: ${profile.goal}
            - Workout Location: ${profile.location}
            - Available Equipment: ${profile.equipment}
            - Workout Days Per Week: ${profile.daysPerWeek}
            - Injuries/Limitations: ${profile.injuries}
            
            Scientific estimations:
            - Maintenance Calories: ${profile.tdee.toInt()} kcal
             
            Requirements for the response:
            - Daily Workout Plan: Create a ${profile.daysPerWeek}-day split workout program tailored specifically to the ${profile.location} location and available equipment.
            - Provide clear, safe target parameters adjusted automatically for their level: ${profile.fitnessLevel}.
            - Ensure ALL exercises are detailed. For each exercise, provide:
              - Name, target muscle groups, sets, reps (such as "10-12" or "15" or "As many reps as possible"), rest duration, difficulty level, estimated calories burned, complete correct form instructions, common mistakes to avoid, and physical benefits.
            - Provide structured Warm-up & Cool-down routine.
            - Provide Weekly Schedule, Monthly Progression Plan, progressive overload recommendations, and specific recovery advice or limitations related directly to their injuries ("${profile.injuries}").
            - Provide custom Nutrition guidance including specific meals: Breakfast, Lunch, Dinner, and Snacks.
            - Provide calculated daily target macros (Protein target, Carbs target, Fat target) and daily water intake in Litres. Ensure total calories align with their Goal.
              - If Goal is Weight Loss/Fat Loss: caloric target should be a safe deficit (~300-500 kcal under TDEE).
              - If Goal is Muscle Gain/Strength: caloric target should be a moderate surplus (~250-400 kcal above TDEE).
              - If Goal is Endurance: maintenance or minor deficit/surplus depending on weight.
            
            You MUST return ONLY a JSON response exactly matching this schema. Avoid any extra chat, markdown codeblocks (do NOT wrap in ```json), or explanatory text before/after.
            
            Schema structure:
            {
              "goal": "${profile.goal}",
              "daily_calories": 2800, // calculated target calories for goal
              "protein": "180g",
              "carbs": "320g",
              "fat": "80g",
              "workout_plan": [
                {
                  "day": "Monday",
                  "focus": "Chest & Triceps",
                  "exercises": [
                    {
                      "name": "Push-Ups",
                      "target": "Chest, Triceps, Shoulders",
                      "sets": 4,
                      "reps": "12",
                      "rest": "60 seconds",
                      "difficulty": "Beginner",
                      "calories": 40,
                      "instructions": [
                        "Keep body straight.",
                        "Lower chest to floor.",
                        "Push back up."
                      ],
                      "mistakes": [
                        "Sagging hips.",
                        "Incomplete range of motion."
                      ],
                      "benefits": [
                        "Builds chest strength.",
                        "Improves upper-body endurance."
                      ]
                    }
                  ]
                }
              ],
              "monthly_progression": "A comprehensive monthly stage description...",
              "progressive_overload": "Specific overload advice...",
              "recovery_advice": "Detailed active recovery metrics and warning notes...",
              "warm_up": "Precise sequence of warm-up drills...",
              "cool_down": "Precise cool-down stretches...",
              "nutrition": {
                "breakfast": "Meal details, protein, calorie content",
                "lunch": "Meal details, protein, calorie content",
                "dinner": "Meal details, protein, calorie content",
                "snacks": "Snack details, protein, calorie content",
                "water_target": "3.5L"
              }
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.6
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(key, request)
            var responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("AI returned an empty response. Please try again."))
            
            // Cleanup potential markdown fences if retrofitted model failed strict adherence
            responseText = responseText.trim()
            if (responseText.startsWith("```json")) {
                responseText = responseText.removePrefix("```json")
            }
            if (responseText.startsWith("```")) {
                responseText = responseText.removePrefix("```")
            }
            if (responseText.endsWith("```")) {
                responseText = responseText.removeSuffix("```")
            }
            responseText = responseText.trim()

            val plan = planAdapter.fromJson(responseText)
                ?: return@withContext Result.failure(Exception("Failed to parse the generated plan. Please try again."))

            // Persist generated plan
            val planEntity = LatestPlanEntity(
                goal = plan.goal,
                dailyCalories = plan.daily_calories,
                protein = plan.protein,
                carbs = plan.carbs,
                fat = plan.fat,
                rawJsonStr = responseText
            )
            fitnessDao.insertLatestPlan(planEntity)

            Result.success(plan)
        } catch (e: Exception) {
            Log.e("FitnessRepository", "Network Error: ", e)
            Result.failure(e)
        }
    }

    // Optional AI dynamic coach recommendations based on logs
    suspend fun generateProgressReview(logs: List<ProgressLogEntity>, prs: List<PersonalRecordEntity>, profile: UserProfile): Result<String> = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured."))
        }

        val logsSummary = logs.take(6).joinToString("\n") { 
            "- Date: ${it.date}, Weight: ${it.weight} kg, BMI: ${it.bmi}, Body Fat: ${it.bodyFat}%, Calories Burned: ${it.caloriesBurned} kcal, Notes: ${it.note}"
        }
        val prSummary = prs.take(6).joinToString("\n") {
            "- ${it.exerciseName}: ${it.weight} kg x ${it.reps} reps on ${it.date}"
        }

        val coachPrompt = """
            As a certified fitness coach, analyze my progress logs, personal records (PRs), and profile.
            Provide detailed progress insights, custom motivation, and actionable training/diet recommendations.
            
            User Details:
            - Goal: ${profile.goal}
            - Fitness Level: ${profile.fitnessLevel}
            - Current Weight: ${profile.weight} kg, Height: ${profile.height} cm (BMI: ${profile.bmi})
            
            Weight & Training Log Entries (Last 6):
            $logsSummary
            
            Personal Records (PRs):
            $prSummary
            
            Keep your feedback professional, encouraging, scientifically grounded, and formatted into short bullet points under headers:
            📈 PROGRESS STATUS
            🥗 NUTRITIONAL FEEDBACK
            🏋️ TRAINING TIPS & PROGRESSIVE OVERLOAD
            🌟 COACH MOTIVATION
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = coachPrompt)))),
            generationConfig = GenerationConfig(temperature = 0.7)
        )

        try {
            val response = RetrofitClient.service.generateContent(key, request)
            val feedback = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Review unavailable. Keep up the high effort!"
            Result.success(feedback)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
