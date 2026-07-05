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
    @Json(name = "calorie_goal") val calorieGoal: Int = 2000,
    @Json(name = "height_cm") val heightCm: Double? = null,
    @Json(name = "age") val age: Int? = null,
    @Json(name = "sex") val sex: String? = null,
    @Json(name = "current_weight_kg") val currentWeightKg: Double? = null,
    @Json(name = "goal_weight_kg") val goalWeightKg: Double? = null,
    @Json(name = "weekly_goal_kg") val weeklyGoalKg: Double? = null,
    @Json(name = "activity_level") val activityLevel: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "calorie_goal") val calorieGoal: Int? = null,
    @Json(name = "height_cm") val heightCm: Double? = null,
    @Json(name = "age") val age: Int? = null,
    @Json(name = "sex") val sex: String? = null,
    @Json(name = "current_weight_kg") val currentWeightKg: Double? = null,
    @Json(name = "goal_weight_kg") val goalWeightKg: Double? = null,
    @Json(name = "weekly_goal_kg") val weeklyGoalKg: Double? = null,
    @Json(name = "activity_level") val activityLevel: String? = null
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
    @Json(name = "calorie_range") val calorieRange: List<Double>,
    @Json(name = "matched_description") val assumption: String? = null,
    @Json(name = "estimated_grams") val estimatedGrams: Double? = null,
    @Json(name = "protein_g") val proteinG: Double? = null,
    @Json(name = "carbs_g") val carbsG: Double? = null,
    @Json(name = "fat_g") val fatG: Double? = null,
    @Json(name = "source_label") val sourceLabel: String? = null
)

@JsonClass(generateAdapter = true)
data class EstimateResponse(
    @Json(name = "note") val note: String,
    @Json(name = "items") val items: List<FoodItem>,
    @Json(name = "total_calories") val totalCalories: Double,
    @Json(name = "total_calorie_range") val totalCalorieRange: List<Double>,
    @Json(name = "total_protein_g") val totalProteinG: Double? = null,
    @Json(name = "total_carbs_g") val totalCarbsG: Double? = null,
    @Json(name = "total_fat_g") val totalFatG: Double? = null,
    @Json(name = "sources") val sources: List<String> = emptyList(),
    @Json(name = "web_grounded") val webGrounded: Boolean = false
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

@JsonClass(generateAdapter = true)
data class PatchCaloriesRequest(
    @Json(name = "total_calories") val totalCalories: Double
)

// ─── Saved Meals ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class SavedMeal(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "calories") val calories: Double
)

@JsonClass(generateAdapter = true)
data class SavedMealRequest(
    @Json(name = "name") val name: String,
    @Json(name = "calories") val calories: Double
)

@JsonClass(generateAdapter = true)
data class QuickLogRequest(
    @Json(name = "name") val name: String,
    @Json(name = "calories") val calories: Double
)

// ─── Barcode log request ─────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class BarcodeLogRequest(
    @Json(name = "note") val note: String,
    @Json(name = "total_calories") val totalCalories: Float,
    @Json(name = "total_protein_g") val totalProteinG: Float = 0f,
    @Json(name = "total_carbs_g") val totalCarbsG: Float = 0f,
    @Json(name = "total_fat_g") val totalFatG: Float = 0f
)

// ─── Barcode ─────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class BarcodeProduct(
    @Json(name = "barcode") val barcode: String,
    @Json(name = "name") val name: String,
    @Json(name = "brand") val brand: String? = null,
    @Json(name = "serving_size_g") val servingSizeG: Float,
    @Json(name = "serving_description") val servingDescription: String? = null,
    @Json(name = "calories_per_serving") val caloriesPerServing: Float,
    @Json(name = "protein_g") val proteinG: Float,
    @Json(name = "carbs_g") val carbsG: Float,
    @Json(name = "fat_g") val fatG: Float,
    @Json(name = "image_url") val imageUrl: String? = null
)

// ─── Weight ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class WeightEntry(
    @Json(name = "id") val id: Int,
    @Json(name = "weight_kg") val weightKg: Double,
    @Json(name = "logged_at") val loggedAt: String
)

@JsonClass(generateAdapter = true)
data class LogWeightRequest(
    @Json(name = "weight_kg") val weightKg: Double
)
