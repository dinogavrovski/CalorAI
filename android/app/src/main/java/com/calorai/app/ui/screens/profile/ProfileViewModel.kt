package com.calorai.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.local.TokenDataStore
import com.calorai.app.data.remote.models.UpdateProfileRequest
import com.calorai.app.data.remote.models.UserProfile
import com.calorai.app.data.repository.ApiResult
import com.calorai.app.data.repository.AuthRepository
import com.calorai.app.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val profile: UserProfile? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

// Editable biometric form state
data class BiometricForm(
    val heightCm: String = "",
    val age: String = "",
    val sex: String = "",             // "male" | "female" | "other"
    val currentWeightKg: String = "",
    val goalWeightKg: String = "",
    val weeklyGoalKg: String = "0.0", // e.g. "-0.5", "0.5"
    val activityLevel: String = ""    // "sedentary" | "light" | "moderate" | "very_active" | "extra_active"
) {
    val calculatedGoal: Int? get() {
        val w = currentWeightKg.toDoubleOrNull() ?: return null
        val h = heightCm.toDoubleOrNull() ?: return null
        val a = age.toIntOrNull() ?: return null
        val s = sex.takeIf { it.isNotBlank() } ?: return null
        val act = activityLevel.takeIf { it.isNotBlank() } ?: return null
        val weekly = weeklyGoalKg.toDoubleOrNull() ?: 0.0
        return calculateTdee(w, h, a, s, act, weekly)
    }
}

private val ACTIVITY_MULTIPLIERS = mapOf(
    "sedentary" to 1.2,
    "light" to 1.375,
    "moderate" to 1.55,
    "very_active" to 1.725,
    "extra_active" to 1.9
)

private val CALORIE_FLOORS = mapOf("male" to 1500, "female" to 1200, "other" to 1350)

fun calculateTdee(
    weightKg: Double, heightCm: Double, age: Int,
    sex: String, activityLevel: String, weeklyGoalKg: Double
): Int {
    val bmr = when (sex.lowercase()) {
        "male" -> 10 * weightKg + 6.25 * heightCm - 5 * age + 5
        "female" -> 10 * weightKg + 6.25 * heightCm - 5 * age - 161
        else -> {
            val m = 10 * weightKg + 6.25 * heightCm - 5 * age + 5
            val f = 10 * weightKg + 6.25 * heightCm - 5 * age - 161
            (m + f) / 2
        }
    }
    val multiplier = ACTIVITY_MULTIPLIERS[activityLevel] ?: 1.2
    val tdee = bmr * multiplier
    val adjustment = weeklyGoalKg * 7700 / 7
    val target = tdee + adjustment
    val floor = CALORIE_FLOORS[sex.lowercase()] ?: 1350
    return (maxOf(target, floor.toDouble()) / 50).roundToInt() * 50
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _biometricForm = MutableStateFlow(BiometricForm())
    val biometricForm: StateFlow<BiometricForm> = _biometricForm.asStateFlow()

    val calorieBias: StateFlow<Float> = tokenDataStore.calorieBias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = mealRepository.getProfile()) {
                is ApiResult.Success -> {
                    val p = result.data
                    _uiState.update { it.copy(isLoading = false, profile = p) }
                    _biometricForm.update {
                        BiometricForm(
                            heightCm = p.heightCm?.toString() ?: "",
                            age = p.age?.toString() ?: "",
                            sex = p.sex ?: "",
                            currentWeightKg = p.currentWeightKg?.toString() ?: "",
                            goalWeightKg = p.goalWeightKg?.toString() ?: "",
                            weeklyGoalKg = p.weeklyGoalKg?.toString() ?: "0.0",
                            activityLevel = p.activityLevel ?: ""
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateForm(form: BiometricForm) {
        _biometricForm.update { form }
    }

    fun saveBiometrics() {
        val form = _biometricForm.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = UpdateProfileRequest(
                heightCm = form.heightCm.toDoubleOrNull(),
                age = form.age.toIntOrNull(),
                sex = form.sex.takeIf { it.isNotBlank() },
                currentWeightKg = form.currentWeightKg.toDoubleOrNull(),
                goalWeightKg = form.goalWeightKg.toDoubleOrNull(),
                weeklyGoalKg = form.weeklyGoalKg.toDoubleOrNull(),
                activityLevel = form.activityLevel.takeIf { it.isNotBlank() }
            )
            when (val result = mealRepository.updateProfile(request)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSaving = false, profile = result.data, successMessage = "Goals updated!")
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun saveDisplayName(name: String) {
        val trimmed = name.trim()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = mealRepository.updateProfile(UpdateProfileRequest(displayName = trimmed))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSaving = false, profile = result.data, successMessage = "Name updated!")
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun saveManualCalorieGoal(goal: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = mealRepository.updateProfile(UpdateProfileRequest(calorieGoal = goal))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSaving = false, profile = result.data, successMessage = "Goal updated!")
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun setCalorieBias(bias: Float) {
        viewModelScope.launch { tokenDataStore.setCalorieBias(bias) }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
