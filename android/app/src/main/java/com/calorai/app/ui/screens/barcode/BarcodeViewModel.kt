package com.calorai.app.ui.screens.barcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.remote.ApiService
import com.calorai.app.data.remote.models.BarcodeLogRequest
import com.calorai.app.data.remote.models.BarcodeProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BarcodeUiState(
    val isLoading: Boolean = false,
    val product: BarcodeProduct? = null,
    val servings: Float = 1f,
    val error: String? = null,
    val isLogging: Boolean = false,
    val logSuccess: Boolean = false
)

@HiltViewModel
class BarcodeViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BarcodeUiState())
    val uiState: StateFlow<BarcodeUiState> = _uiState.asStateFlow()

    fun lookupBarcode(barcode: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getBarcodeProduct(barcode)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update { it.copy(isLoading = false, product = response.body(), servings = 1f) }
                } else if (response.code() == 404) {
                    _uiState.update { it.copy(isLoading = false, error = "Product not found — try another barcode") }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Lookup failed (${response.code()})") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.localizedMessage}") }
            }
        }
    }

    fun setServings(servings: Float) {
        _uiState.update { it.copy(servings = servings) }
    }

    fun logProduct(servings: Float) {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLogging = true) }
            try {
                val request = BarcodeLogRequest(
                    note = buildDescription(product, servings),
                    totalCalories = product.caloriesPerServing * servings,
                    totalProteinG = product.proteinG * servings,
                    totalCarbsG = product.carbsG * servings,
                    totalFatG = product.fatG * servings
                )
                val response = apiService.logBarcodeProduct(request)
                _uiState.update { it.copy(isLogging = false, logSuccess = response.isSuccessful) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLogging = false, error = "Failed to log: ${e.localizedMessage}") }
            }
        }
    }

    fun clearProduct() {
        _uiState.update { it.copy(product = null, error = null, servings = 1f, logSuccess = false) }
    }

    private fun buildDescription(product: BarcodeProduct, servings: Float): String {
        val brand = if (product.brand != null) "${product.brand} " else ""
        val qty = if (servings == 1f) "" else "%.1f servings of ".format(servings)
        return "$qty$brand${product.name}"
    }
}
