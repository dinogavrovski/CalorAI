package com.calorai.app.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Auth ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String
)

// ─── User ────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String,
    @Json(name = "name") val name: String,
    @Json(name = "calorie_goal") val calorieGoal: Int = 2000,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "calorie_goal") val calorieGoal: Int? = null
)

// ─── AI Estimate ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class EstimateRequest(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class FoodItem(
    @Json(name = "name") val name: String,
    @Json(name = "calories_min") val caloriesMin: Int,
    @Json(name = "calories_max") val caloriesMax: Int
)

@JsonClass(generateAdapter = true)
data class EstimateResponse(
    @Json(name = "items") val items: List<FoodItem>,
    @Json(name = "total_min") val totalMin: Int,
    @Json(name = "total_max") val totalMax: Int
)

// ─── Meals ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class LogMealRequest(
    @Json(name = "text") val text: String,
    @Json(name = "items") val items: List<FoodItem>,
    @Json(name = "total_min") val totalMin: Int,
    @Json(name = "total_max") val totalMax: Int
)

@JsonClass(generateAdapter = true)
data class MealLog(
    @Json(name = "id") val id: String,
    @Json(name = "text") val text: String,
    @Json(name = "items") val items: List<FoodItem>,
    @Json(name = "total_min") val totalMin: Int,
    @Json(name = "total_max") val totalMax: Int,
    @Json(name = "logged_at") val loggedAt: String
)

@JsonClass(generateAdapter = true)
data class MealHistoryResponse(
    @Json(name = "meals") val meals: List<MealLog>,
    @Json(name = "total_calories_today_min") val totalCaloriesTodayMin: Int = 0,
    @Json(name = "total_calories_today_max") val totalCaloriesTodayMax: Int = 0
)
