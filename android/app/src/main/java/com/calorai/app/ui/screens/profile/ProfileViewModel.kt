package com.calorai.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.remote.models.UserProfile
import com.calorai.app.data.repository.ApiResult
import com.calorai.app.data.repository.AuthRepository
import com.calorai.app.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val profile: UserProfile? = null,
    val editName: String = "",
    val editCalorieGoal: String = "",
    val isEditing: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = mealRepository.getProfile()) {
                is ApiResult.Success -> {
                    val profile = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            editName = profile.name,
                            editCalorieGoal = profile.calorieGoal.toString()
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleEdit() {
        val profile = _uiState.value.profile ?: return
        _uiState.update {
            it.copy(
                isEditing = !it.isEditing,
                editName = profile.name,
                editCalorieGoal = profile.calorieGoal.toString(),
                errorMessage = null
            )
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(editName = name) }
    }

    fun updateCalorieGoal(goal: String) {
        if (goal.all { it.isDigit() } || goal.isEmpty()) {
            _uiState.update { it.copy(editCalorieGoal = goal) }
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        val goal = state.editCalorieGoal.toIntOrNull()
        if (goal != null && (goal < 500 || goal > 10000)) {
            _uiState.update { it.copy(errorMessage = "Calorie goal must be between 500 and 10,000") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = mealRepository.updateProfile(
                name = state.editName.trim().ifBlank { null },
                calorieGoal = goal
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        profile = result.data,
                        successMessage = "Profile updated!"
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
