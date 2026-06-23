package com.calorai.app.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.remote.models.EstimateResponse
import com.calorai.app.data.repository.ApiResult
import com.calorai.app.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val errorMessage: String? = null
)

@HiltViewModel
class LogMealViewModel @Inject constructor(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogMealUiState())
    val uiState: StateFlow<LogMealUiState> = _uiState.asStateFlow()

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

    fun goBack() {
        _uiState.update { it.copy(step = LogStep.INPUT, errorMessage = null) }
    }

    fun reset() {
        _uiState.value = LogMealUiState()
    }
}
