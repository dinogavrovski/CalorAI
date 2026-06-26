package com.calorai.app.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.local.TokenDataStore
import com.calorai.app.data.remote.models.EstimateResponse
import com.calorai.app.data.remote.models.SavedMeal
import com.calorai.app.data.repository.ApiResult
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

enum class LogStep { INPUT, RESULT, LOGGING, SUCCESS }

data class LogMealUiState(
    val step: LogStep = LogStep.INPUT,
    val mealText: String = "",
    val estimate: EstimateResponse? = null,
    val isEstimating: Boolean = false,
    val isLogging: Boolean = false,
    val errorMessage: String? = null,
    val savedMeals: List<SavedMeal> = emptyList(),
    val isSavingMeal: Boolean = false,
    val mealSavedSuccess: Boolean = false
)

@HiltViewModel
class LogMealViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogMealUiState())
    val uiState: StateFlow<LogMealUiState> = _uiState.asStateFlow()

    val calorieBias: StateFlow<Float> = tokenDataStore.calorieBias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    init { loadSavedMeals() }

    fun updateMealText(text: String) {
        _uiState.update { it.copy(mealText = text, errorMessage = null) }
    }

    fun estimateMeal() {
        val text = _uiState.value.mealText.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please describe your meal") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isEstimating = true, errorMessage = null) }
            when (val result = mealRepository.estimateMeal(text)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isEstimating = false, estimate = result.data, step = LogStep.RESULT)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isEstimating = false, errorMessage = result.message)
                }
                else -> _uiState.update { it.copy(isEstimating = false) }
            }
        }
    }

    fun confirmAndLog() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLogging = true, errorMessage = null, step = LogStep.LOGGING) }
            when (val result = mealRepository.logMeal(state.mealText)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLogging = false, step = LogStep.SUCCESS) }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLogging = false, errorMessage = result.message, step = LogStep.RESULT)
                }
                else -> _uiState.update { it.copy(isLogging = false, step = LogStep.RESULT) }
            }
        }
    }

    fun saveCurrentMeal() {
        val state = _uiState.value
        val estimate = state.estimate ?: return
        val name = state.mealText.take(80)
        val bias = calorieBias.value
        val calories = (estimate.totalCalories * (1f + bias)).coerceAtLeast(1.0)
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingMeal = true) }
            when (val result = mealRepository.createSavedMeal(name, calories)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSavingMeal = false,
                        mealSavedSuccess = true,
                        savedMeals = listOf(result.data) + it.savedMeals
                    )
                }
                else -> _uiState.update { it.copy(isSavingMeal = false) }
            }
        }
    }

    fun quickLogSavedMeal(savedMeal: SavedMeal) {
        viewModelScope.launch {
            _uiState.update { it.copy(step = LogStep.LOGGING) }
            when (mealRepository.quickLogMeal(savedMeal.name, savedMeal.calories)) {
                is ApiResult.Success -> _uiState.update { it.copy(step = LogStep.SUCCESS) }
                is ApiResult.Error -> _uiState.update { it.copy(step = LogStep.INPUT) }
                else -> _uiState.update { it.copy(step = LogStep.INPUT) }
            }
        }
    }

    fun deleteSavedMeal(id: Int) {
        viewModelScope.launch {
            mealRepository.deleteSavedMeal(id)
            _uiState.update { it.copy(savedMeals = it.savedMeals.filter { m -> m.id != id }) }
        }
    }

    fun loadSavedMeals() {
        viewModelScope.launch {
            when (val result = mealRepository.getSavedMeals()) {
                is ApiResult.Success -> _uiState.update { it.copy(savedMeals = result.data) }
                else -> Unit
            }
        }
    }

    fun clearMealSavedSuccess() {
        _uiState.update { it.copy(mealSavedSuccess = false) }
    }

    fun goBack() {
        _uiState.update { it.copy(step = LogStep.INPUT, errorMessage = null) }
    }

    fun reset() {
        _uiState.value = LogMealUiState(savedMeals = _uiState.value.savedMeals)
    }
}
