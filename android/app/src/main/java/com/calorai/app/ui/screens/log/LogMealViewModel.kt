package com.calorai.app.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.local.TokenDataStore
import com.calorai.app.data.remote.models.BarcodeLogItem
import com.calorai.app.data.remote.models.EstimateResponse
import com.calorai.app.data.remote.models.FoodItem
import com.calorai.app.data.remote.models.LoggedBarcodeItem
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
    val barcodeItems: List<LoggedBarcodeItem> = emptyList(),
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

    fun addBarcodeItem(item: LoggedBarcodeItem) {
        _uiState.update { it.copy(barcodeItems = it.barcodeItems + item, errorMessage = null) }
    }

    fun removeBarcodeItem(index: Int) {
        _uiState.update { state ->
            state.copy(barcodeItems = state.barcodeItems.filterIndexed { i, _ -> i != index })
        }
    }

    fun estimateMeal() {
        val text = _uiState.value.mealText.trim()
        val barcodeItems = _uiState.value.barcodeItems
        // The AI endpoint needs at least 2 chars; shorter text is treated as "no typed part".
        val hasTypedMeal = text.length >= 2
        if (!hasTypedMeal && barcodeItems.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Describe your meal or scan an item") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isEstimating = true, errorMessage = null) }

            // Barcode-only: no typed text → skip the AI entirely, build result from exact data
            if (!hasTypedMeal) {
                _uiState.update {
                    it.copy(
                        isEstimating = false,
                        estimate = buildBarcodeOnlyEstimate(barcodeItems),
                        step = LogStep.RESULT
                    )
                }
                return@launch
            }

            when (val result = mealRepository.estimateMeal(text)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isEstimating = false,
                        estimate = mergeBarcodeItems(result.data, barcodeItems),
                        step = LogStep.RESULT
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isEstimating = false, errorMessage = result.message)
                }
                else -> _uiState.update { it.copy(isEstimating = false) }
            }
        }
    }

    /** Turn a scanned item into a synthetic [FoodItem] with exact, tight-range nutrition. */
    private fun LoggedBarcodeItem.toFoodItem(): FoodItem = FoodItem(
        name = name,
        calories = calories.toDouble(),
        calorieRange = listOf(calories * 0.98, calories * 1.02).map { it.toDouble() },
        assumption = "Scanned product — exact nutrition from the barcode label",
        estimatedGrams = null,
        proteinG = proteinG.toDouble(),
        carbsG = carbsG.toDouble(),
        fatG = fatG.toDouble(),
        sourceLabel = "Barcode"
    )

    /** Append exact barcode items onto an AI estimate and re-sum the totals. */
    private fun mergeBarcodeItems(
        ai: EstimateResponse,
        barcodeItems: List<LoggedBarcodeItem>
    ): EstimateResponse {
        if (barcodeItems.isEmpty()) return ai
        val bCal = barcodeItems.sumOf { it.calories.toDouble() }
        val aiLow = ai.totalCalorieRange.getOrElse(0) { ai.totalCalories }
        val aiHigh = ai.totalCalorieRange.getOrElse(1) { ai.totalCalories }
        return ai.copy(
            items = ai.items + barcodeItems.map { it.toFoodItem() },
            totalCalories = ai.totalCalories + bCal,
            totalCalorieRange = listOf(aiLow + bCal * 0.98, aiHigh + bCal * 1.02),
            totalProteinG = (ai.totalProteinG ?: 0.0) + barcodeItems.sumOf { it.proteinG.toDouble() },
            totalCarbsG = (ai.totalCarbsG ?: 0.0) + barcodeItems.sumOf { it.carbsG.toDouble() },
            totalFatG = (ai.totalFatG ?: 0.0) + barcodeItems.sumOf { it.fatG.toDouble() }
        )
    }

    /** Build a full estimate purely from scanned items when nothing was typed. */
    private fun buildBarcodeOnlyEstimate(barcodeItems: List<LoggedBarcodeItem>): EstimateResponse {
        val bCal = barcodeItems.sumOf { it.calories.toDouble() }
        return EstimateResponse(
            note = barcodeItems.joinToString(", ") { it.name },
            items = barcodeItems.map { it.toFoodItem() },
            totalCalories = bCal,
            totalCalorieRange = listOf(bCal * 0.98, bCal * 1.02),
            totalProteinG = barcodeItems.sumOf { it.proteinG.toDouble() },
            totalCarbsG = barcodeItems.sumOf { it.carbsG.toDouble() },
            totalFatG = barcodeItems.sumOf { it.fatG.toDouble() },
            sources = emptyList(),
            webGrounded = false
        )
    }

    fun confirmAndLog() {
        val state = _uiState.value
        val barcodeReq = state.barcodeItems.map {
            BarcodeLogItem(
                note = it.name,
                calories = it.calories,
                proteinG = it.proteinG,
                carbsG = it.carbsG,
                fatG = it.fatG
            )
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLogging = true, errorMessage = null, step = LogStep.LOGGING) }
            when (val result = mealRepository.logMeal(state.mealText, barcodeReq)) {
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
        val name = state.mealText.take(80).ifBlank { estimate.note.take(80) }
        val bias = calorieBias.value
        // Bias corrects AI guessing only — exact barcode calories pass through untouched.
        val exactCalories = estimate.items.filter { it.sourceLabel == "Barcode" }.sumOf { it.calories }
        val aiCalories = (estimate.totalCalories - exactCalories).coerceAtLeast(0.0)
        val calories = (aiCalories * (1f + bias) + exactCalories).coerceAtLeast(1.0)
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
