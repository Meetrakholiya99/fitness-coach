package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PersonalRecordEntity
import com.example.data.local.ProgressLogEntity
import com.example.data.local.StreakStatsEntity
import com.example.data.model.GeneratedPlan
import com.example.data.model.UserProfile
import com.example.data.repository.FitnessRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitnessRepository

    // States
    val userProfile: StateFlow<UserProfile>
    val latestPlan: StateFlow<GeneratedPlan?>
    val progressLogs: StateFlow<List<ProgressLogEntity>>
    val prs: StateFlow<List<PersonalRecordEntity>>
    val streakStats: StateFlow<StreakStatsEntity?>

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    private val _isReviewing = MutableStateFlow(false)
    val isReviewing: StateFlow<Boolean> = _isReviewing.asStateFlow()

    private val _coachReview = MutableStateFlow<String?>(null)
    val coachReview: StateFlow<String?> = _coachReview.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FitnessRepository(database.fitnessDao())

        // Map flows with sensible defaults if database is empty
        userProfile = repository.userProfile
            .map { it ?: UserProfile() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

        latestPlan = repository.latestPlan
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        progressLogs = repository.progressLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        prs = repository.personalRecords
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        streakStats = repository.streakStats
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun clearGenerationError() {
        _generationError.value = null
    }

    fun generatePlan() {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationError.value = null
            
            val currentProfile = userProfile.value
            val result = repository.generateWorkoutAndNutritionPlan(currentProfile)
            
            result.onSuccess {
                _generationError.value = null
            }.onFailure { exception ->
                _generationError.value = exception.message ?: "Failed to generate plan. Please try again."
            }
            _isGenerating.value = false
        }
    }

    fun addProgressLog(weight: Double, bodyFat: Double, caloriesBurned: Double, note: String) {
        viewModelScope.launch {
            val profile = userProfile.value
            // Calculate BMI dynamically
            val heightMeters = profile.height / 100.0
            val calculatedBmi = if (heightMeters > 0) weight / (heightMeters * heightMeters) else 0.0
            
            repository.logProgress(
                weight = weight,
                bodyFat = bodyFat,
                bmi = calculatedBmi,
                caloriesBurned = caloriesBurned,
                note = note
            )
            
            // Also update weight in user profile so calculations stay in sync
            val updatedProfile = profile.copy(weight = weight)
            repository.saveUserProfile(updatedProfile)
        }
    }

    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteLog(id)
        }
    }

    fun addPR(exerciseName: String, weight: Double, reps: Int) {
        viewModelScope.launch {
            repository.logPR(exerciseName, weight, reps)
        }
    }

    fun deletePR(id: Int) {
        viewModelScope.launch {
            repository.deletePR(id)
        }
    }

    fun completeWorkout(caloriesBurned: Double) {
        viewModelScope.launch {
            repository.completeWorkout(caloriesBurned)
        }
    }

    fun getCoachReview() {
        viewModelScope.launch {
            _isReviewing.value = true
            _coachReview.value = null
            
            val currentProfile = userProfile.value
            val currentLogs = progressLogs.value
            val currentPrs = prs.value
            
            val result = repository.generateProgressReview(currentLogs, currentPrs, currentProfile)
            result.onSuccess { text ->
                _coachReview.value = text
            }.onFailure { err ->
                _coachReview.value = "Review failed: ${err.message}. Please verify internet access and your API key."
            }
            
            _isReviewing.value = false
        }
    }

    // Factory Class for ViewModel
    companion object {
        class Factory(private val application: Application) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return FitnessViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
