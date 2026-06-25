package com.calorai.app.ui.screens.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorai.app.data.remote.models.WeightEntry
import com.calorai.app.data.repository.ApiResult
import com.calorai.app.data.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeightUiState(
    val entries: List<WeightEntry> = emptyList(),
    val latest: WeightEntry? = null,
    val period: String = "month",   // "week" | "month" | "year"
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val showInputSheet: Boolean = false
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val repo: WeightRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeightUiState())
    val state: StateFlow<WeightUiState> = _state.asStateFlow()

    init { load() }

    fun setPeriod(period: String) {
        _state.update { it.copy(period = period) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val period = _state.value.period
            val historyResult = repo.getHistory(period)
            val latestResult = repo.getLatest()
            _state.update {
                it.copy(
                    isLoading = false,
                    entries = if (historyResult is ApiResult.Success) historyResult.data else it.entries,
                    latest = if (latestResult is ApiResult.Success) latestResult.data else it.latest,
                    error = if (historyResult is ApiResult.Error) historyResult.message else null
                )
            }
        }
    }

    fun logWeight(kg: Double) {
        if (kg <= 0) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, showInputSheet = false) }
            when (val result = repo.logWeight(kg)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isSaving = false, latest = result.data) }
                    load()
                }
                is ApiResult.Error -> _state.update { it.copy(isSaving = false, error = result.message) }
                else -> _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun showSheet() = _state.update { it.copy(showInputSheet = true) }
    fun hideSheet() = _state.update { it.copy(showInputSheet = false) }
}
