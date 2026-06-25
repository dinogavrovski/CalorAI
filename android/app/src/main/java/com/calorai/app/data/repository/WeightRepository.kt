package com.calorai.app.data.repository

import com.calorai.app.data.remote.ApiService
import com.calorai.app.data.remote.models.LogWeightRequest
import com.calorai.app.data.remote.models.WeightEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun logWeight(kg: Double): ApiResult<WeightEntry> = safeCall {
        apiService.logWeight(LogWeightRequest(kg))
    }

    suspend fun getHistory(period: String): ApiResult<List<WeightEntry>> = safeCall {
        apiService.getWeightHistory(period)
    }

    suspend fun getLatest(): ApiResult<WeightEntry?> = try {
        val r = apiService.getLatestWeight()
        if (r.isSuccessful) ApiResult.Success(r.body())
        else ApiResult.Error("Failed (${r.code()})")
    } catch (e: Exception) {
        ApiResult.Error(e.localizedMessage ?: "Network error")
    }
}

private suspend fun <T> safeCall(block: suspend () -> retrofit2.Response<T>): ApiResult<T> = try {
    val r = block()
    if (r.isSuccessful) ApiResult.Success(r.body()!!)
    else ApiResult.Error("Failed (${r.code()})")
} catch (e: Exception) {
    ApiResult.Error(e.localizedMessage ?: "Network error")
}
