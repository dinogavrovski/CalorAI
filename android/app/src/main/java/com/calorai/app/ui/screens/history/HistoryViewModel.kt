package com.calorai.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.remote.models.MealLog
import com.calorai.app.data.repository.ApiResult
import com.calorai.app.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = false,
    val meals: List<MealLog> = emptyList(),
    val errorMessage: String? = null,
    val savingMealId: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun updateMealCalories(mealId: String, calories: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(savingMealId = mealId) }
            when (val result = mealRepository.patchMealCalories(mealId, calories)) {
                is ApiResult.Success -> _uiState.update { state ->
                    state.copy(
                        savingMealId = null,
                        meals = state.meals.map { if (it.id == mealId) result.data else it }
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(savingMealId = null, errorMessage = result.message)
                }
                else -> _uiState.update { it.copy(savingMealId = null) }
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = mealRepository.getMealHistory()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, meals = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
