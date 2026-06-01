package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val age: Int = 28,
    val gender: String = "Male",
    val height: Double = 175.0, // cm
    val weight: Double = 75.0,  // kg
    val fitnessLevel: String = "Intermediate", // Beginner, Intermediate, Advanced
    val goal: String = "Muscle Gain", // Weight Loss, Muscle Gain, Fat Loss, Strength, Endurance
    val location: String = "Gym", // Home, Gym
    val equipment: String = "Dumbbells, Barbells, Machines",
    val daysPerWeek: Int = 4,
    val injuries: String = "None"
) {
    // Scientific calculations
    val bmr: Double
        get() {
            // Harris-Benedict Equation (Revised)
            return if (gender.equals("Male", ignoreCase = true)) {
                88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
            } else {
                447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
            }
        }

    val tdee: Double
        get() {
            // Activity factors based on workout days
            val multiplier = when {
                daysPerWeek <= 1 -> 1.2
                daysPerWeek <= 3 -> 1.375
                daysPerWeek <= 5 -> 1.55
                else -> 1.725
            }
            return bmr * multiplier
        }

    val bmi: Double
        get() {
            val heightMeters = height / 100.0
            return if (heightMeters > 0) weight / (heightMeters * heightMeters) else 0.0
        }

    val bodyFatEstimate: Double
        get() {
            // US Navy formula approximation / BMI method:
            // Adult Body Fat % = (1.20 × BMI) + (0.23 × Age) - (10.8 × gender) - 5.4
            val genderVal = if (gender.equals("Male", ignoreCase = true)) 1 else 0
            val est = (1.20 * bmi) + (0.23 * age) - (10.8 * genderVal) - 5.4
            return if (est > 2.0) est else 15.0 // Safe default limits
        }
}

@JsonClass(generateAdapter = true)
data class Exercise(
    val name: String,
    val target: String,
    val sets: Int,
    val reps: String,
    val rest: String,
    val difficulty: String,
    val calories: Int,
    val instructions: List<String> = emptyList(),
    val mistakes: List<String> = emptyList(),
    val benefits: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DayWorkout(
    val day: String,
    val focus: String,
    val exercises: List<Exercise> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NutritionPlan(
    val breakfast: String,
    val lunch: String,
    val dinner: String,
    val snacks: String,
    val water_target: String
)

@JsonClass(generateAdapter = true)
data class GeneratedPlan(
    val goal: String,
    val daily_calories: Int,
    val protein: String,
    val carbs: String,
    val fat: String,
    val workout_plan: List<DayWorkout> = emptyList(),
    val monthly_progression: String = "",
    val progressive_overload: String = "",
    val recovery_advice: String = "",
    val warm_up: String = "",
    val cool_down: String = "",
    val nutrition: NutritionPlan? = null
)
