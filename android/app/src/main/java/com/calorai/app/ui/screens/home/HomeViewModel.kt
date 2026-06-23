package com.calorai.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.remote.models.MealLog
import com.calorai.app.data.remote.models.UserProfile
import com.calorai.app.data.repository.ApiResult
import com.calorai.app.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val todayCaloriesMin: Int = 0,
    val todayCaloriesMax: Int = 0,
    val recentMeals: List<MealLog> = emptyList(),
    val errorMessage: String? = null
) {
    val calorieGoal: Int get() = profile?.calorieGoal ?: 2000
    val consumedCalories: Int get() = (todayCaloriesMin + todayCaloriesMax) / 2
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Load profile
            when (val profileResult = mealRepository.getProfile()) {
                is ApiResult.Success -> _uiState.update { it.copy(profile = profileResult.data) }
                is ApiResult.Error -> _uiState.update { it.copy(errorMessage = profileResult.message) }
                else -> {}
            }

            // Load today's meals / history
            when (val historyResult = mealRepository.getMealHistory()) {
                is ApiResult.Success -> {
                    val data = historyResult.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentMeals = data.meals.take(3),
                            todayCaloriesMin = data.totalCaloriesTodayMin,
                            todayCaloriesMax = data.totalCaloriesTodayMax
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = historyResult.message)
                }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
