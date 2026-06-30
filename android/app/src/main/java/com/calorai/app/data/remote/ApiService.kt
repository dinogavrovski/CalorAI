package com.calorai.app.data.remote

import com.calorai.app.data.remote.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // ── User ──────────────────────────────────────────────────────────────────

    @GET("user/profile")
    suspend fun getProfile(): Response<UserProfile>

    @PATCH("user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfile>

    // ── AI ────────────────────────────────────────────────────────────────────

    @POST("ai/log-text")
    suspend fun estimateMeal(@Body request: EstimateRequest): Response<EstimateResponse>

    // ── Meals ─────────────────────────────────────────────────────────────────

    @POST("user/meal-history")
    suspend fun logMeal(@Body request: LogMealRequest): Response<MealLog>

    @GET("user/meal-history")
    suspend fun getMealHistory(): Response<List<MealLog>>

    @PATCH("user/meal-history/{id}")
    suspend fun patchMealCalories(
        @Path("id") id: String,
        @Body request: PatchCaloriesRequest
    ): Response<MealLog>

    // ── Saved Meals ───────────────────────────────────────────────────────────

    @GET("user/saved-meals")
    suspend fun getSavedMeals(): Response<List<SavedMeal>>

    @POST("user/saved-meals")
    suspend fun createSavedMeal(@Body request: SavedMealRequest): Response<SavedMeal>

    @DELETE("user/saved-meals/{id}")
    suspend fun deleteSavedMeal(@Path("id") id: Int): Response<Unit>

    @POST("user/meal-history/quick")
    suspend fun quickLogMeal(@Body request: QuickLogRequest): Response<MealLog>

    // ── Weight ────────────────────────────────────────────────────────────────

    @POST("user/weight")
    suspend fun logWeight(@Body request: LogWeightRequest): Response<WeightEntry>

    @GET("user/weight")
    suspend fun getWeightHistory(@Query("period") period: String): Response<List<WeightEntry>>

    @GET("user/weight/latest")
    suspend fun getLatestWeight(): Response<WeightEntry>
}
