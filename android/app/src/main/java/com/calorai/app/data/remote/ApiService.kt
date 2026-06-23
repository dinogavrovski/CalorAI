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

    @GET("users/me")
    suspend fun getProfile(): Response<UserProfile>

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfile>

    // ── AI ────────────────────────────────────────────────────────────────────

    @POST("ai/estimate")
    suspend fun estimateMeal(@Body request: EstimateRequest): Response<EstimateResponse>

    // ── Meals ─────────────────────────────────────────────────────────────────

    @POST("meals/log")
    suspend fun logMeal(@Body request: LogMealRequest): Response<MealLog>

    @GET("meals/history")
    suspend fun getMealHistory(): Response<MealHistoryResponse>
}
