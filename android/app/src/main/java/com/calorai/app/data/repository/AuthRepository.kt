package com.calorai.app.data.repository

import com.calorai.app.data.local.TokenDataStore
import com.calorai.app.data.remote.ApiService
import com.calorai.app.data.remote.models.AuthResponse
import com.calorai.app.data.remote.models.LoginRequest
import com.calorai.app.data.remote.models.RegisterRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: Int? = null) : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore
) {

    val isLoggedIn: Flow<Boolean> = tokenDataStore.isLoggedIn

    suspend fun login(email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenDataStore.saveTokens(body.accessToken, body.refreshToken)
                AuthResult.Success(body)
            } else {
                AuthResult.Error(
                    message = when (response.code()) {
                        401 -> "Invalid email or password"
                        422 -> "Please check your input"
                        else -> "Login failed (${response.code()})"
                    },
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, name))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenDataStore.saveTokens(body.accessToken, body.refreshToken)
                AuthResult.Success(body)
            } else {
                AuthResult.Error(
                    message = when (response.code()) {
                        409 -> "An account with this email already exists"
                        422 -> "Please check your input"
                        else -> "Registration failed (${response.code()})"
                    },
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun logout() {
        tokenDataStore.clearTokens()
    }
}
