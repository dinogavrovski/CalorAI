package com.calorai.app.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Auth ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "username") val username: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username") val username: String,
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
    @Json(name = "calorie_goal") val calorieGoal: Int = 2000
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "calorie_goal") val calorieGoal: Int? = null
)

// ─── AI Estimate ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class EstimateRequest(
    @Json(name = "note") val note: String
)

@JsonClass(generateAdapter = true)
data class FoodItem(
    @Json(name = "parsed_food") val name: String,
    @Json(name = "calories") val calories: Double,
    @Json(name = "calorie_range") val calorieRange: List<Double>
)

@JsonClass(generateAdapter = true)
data class EstimateResponse(
    @Json(name = "note") val note: String,
    @Json(name = "items") val items: List<FoodItem>,
    @Json(name = "total_calories") val totalCalories: Double,
    @Json(name = "total_calorie_range") val totalCalorieRange: List<Double>
)

// ─── Meals ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class LogMealRequest(
    @Json(name = "note") val note: String
)

@JsonClass(generateAdapter = true)
data class MealLog(
    @Json(name = "id") val id: String,
    @Json(name = "note") val note: String,
    @Json(name = "items") val items: List<FoodItem>,
    @Json(name = "total_calories") val totalCalories: Double,
    @Json(name = "total_calorie_range") val totalCalorieRange: List<Double>,
    @Json(name = "timestamp") val timestamp: String
)
