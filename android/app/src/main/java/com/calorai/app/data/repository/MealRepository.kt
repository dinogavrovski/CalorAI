package com.calorai.app.data.repository

import com.calorai.app.data.remote.ApiService
import com.calorai.app.data.remote.models.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

@Singleton
class MealRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun estimateMeal(description: String): ApiResult<EstimateResponse> {
        return try {
            val response = apiService.estimateMeal(EstimateRequest(description))
            if (response.isSuccessful) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Estimation failed (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun logMeal(note: String): ApiResult<MealLog> {
        return try {
            val response = apiService.logMeal(LogMealRequest(note))
            if (response.isSuccessful) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to log meal (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getMealHistory(): ApiResult<List<MealLog>> {
        return try {
            val response = apiService.getMealHistory()
            if (response.isSuccessful) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to fetch history (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getProfile(): ApiResult<UserProfile> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to fetch profile (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun updateProfile(calorieGoal: Int?): ApiResult<UserProfile> {
        return try {
            val response = apiService.updateProfile(UpdateProfileRequest(calorieGoal))
            if (response.isSuccessful) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Failed to update profile (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }
}
