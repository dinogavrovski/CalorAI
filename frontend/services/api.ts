import axios, { AxiosInstance, AxiosError } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as SecureStore from 'expo-secure-store';
import { API_CONFIG } from '../constants/config';
import { TextLogResponse, AuthResponse, User, MealHistory } from '../types';

type SaveMealHistoryInput = TextLogResponse | string;

interface RefreshResponse {
  access_token: string;
  refresh_token: string;
  access_token_expires_in: number;
  token_type: string;
}

const AUTH_TOKEN_KEY = 'authToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_KEY = 'user';

class ApiService {
  private api: AxiosInstance;
  private token: string | null = null;
  private refreshPromise: Promise<string | null> | null = null;
  private textEstimateCache = new Map<string, { data: TextLogResponse; ts: number }>();

  private async persistAuth(auth: AuthResponse): Promise<void> {
    this.token = auth.access_token;
    await this.setSensitiveItem(AUTH_TOKEN_KEY, this.token);
    await this.setSensitiveItem(REFRESH_TOKEN_KEY, auth.refresh_token);
    await AsyncStorage.setItem(USER_KEY, JSON.stringify(auth.user));
  }

  private async getSensitiveItem(key: string): Promise<string | null> {
    try {
      const value = await SecureStore.getItemAsync(key);
      if (value !== null) {
        return value;
      }
    } catch {
      // Fallback for environments where SecureStore is unavailable.
    }
    return AsyncStorage.getItem(key);
  }

  private async setSensitiveItem(key: string, value: string): Promise<void> {
    try {
      await SecureStore.setItemAsync(key, value);
      await AsyncStorage.removeItem(key);
      return;
    } catch {
      // Fallback for environments where SecureStore is unavailable.
    }
    await AsyncStorage.setItem(key, value);
  }

  private async deleteSensitiveItem(key: string): Promise<void> {
    try {
      await SecureStore.deleteItemAsync(key);
    } catch {
      // Ignore errors so AsyncStorage cleanup still runs.
    }
    await AsyncStorage.removeItem(key);
  }

  private async clearAuthStorage(): Promise<void> {
    this.token = null;
    await this.deleteSensitiveItem(AUTH_TOKEN_KEY);
    await this.deleteSensitiveItem(REFRESH_TOKEN_KEY);
    await AsyncStorage.removeItem(USER_KEY);
  }

  private async refreshAccessToken(): Promise<string | null> {
    const refreshToken = await this.getSensitiveItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      await this.clearAuthStorage();
      return null;
    }

    try {
      const response = await this.api.post<RefreshResponse>(API_CONFIG.ENDPOINTS.AUTH.REFRESH, {
        refresh_token: refreshToken,
      });

      this.token = response.data.access_token;
      await this.setSensitiveItem(AUTH_TOKEN_KEY, response.data.access_token);
      await this.setSensitiveItem(REFRESH_TOKEN_KEY, response.data.refresh_token);

      return response.data.access_token;
    } catch {
      await this.clearAuthStorage();
      return null;
    }
  }

  constructor() {
    this.api = axios.create({
      baseURL: API_CONFIG.BASE_URL,
      timeout: 10000,
    });

    // Add request interceptor to include auth token
    this.api.interceptors.request.use(
      async (config) => {
        const token = await this.getSensitiveItem(AUTH_TOKEN_KEY);
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Add response interceptor for error handling
    this.api.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as (typeof error.config & { _retry?: boolean }) | undefined;
        const isAuthRefreshCall = originalRequest?.url?.includes(API_CONFIG.ENDPOINTS.AUTH.REFRESH);

        if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isAuthRefreshCall) {
          originalRequest._retry = true;

          if (!this.refreshPromise) {
            this.refreshPromise = this.refreshAccessToken();
          }

          const newAccessToken = await this.refreshPromise;
          this.refreshPromise = null;

          if (newAccessToken) {
            originalRequest.headers = originalRequest.headers ?? {};
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return this.api(originalRequest);
          }
        }

        return Promise.reject(error);
      }
    );
  }

  // Auth Methods
  async register(username: string, email: string, password: string): Promise<AuthResponse> {
    const response = await this.api.post<AuthResponse>(
      API_CONFIG.ENDPOINTS.AUTH.REGISTER,
      { username, email, password }
    );
    await this.persistAuth(response.data);
    return response.data;
  }

  async login(username: string, password: string): Promise<AuthResponse> {
    const response = await this.api.post<AuthResponse>(
      API_CONFIG.ENDPOINTS.AUTH.LOGIN,
      { username, password }
    );
    await this.persistAuth(response.data);
    return response.data;
  }

  async loginWithGoogle(idToken: string): Promise<AuthResponse> {
    const response = await this.api.post<AuthResponse>(
      API_CONFIG.ENDPOINTS.AUTH.GOOGLE,
      { id_token: idToken }
    );
    await this.persistAuth(response.data);
    return response.data;
  }

  async logout(): Promise<void> {
    const refreshToken = await this.getSensitiveItem(REFRESH_TOKEN_KEY);
    if (refreshToken) {
      try {
        await this.api.post(API_CONFIG.ENDPOINTS.AUTH.LOGOUT, {
          refresh_token: refreshToken,
        });
      } catch {
        // Clear local auth regardless of network failures during logout.
      }
    }
    await this.clearAuthStorage();
  }

  async getStoredToken(): Promise<string | null> {
    return this.getSensitiveItem(AUTH_TOKEN_KEY);
  }

  async getStoredUser(): Promise<User | null> {
    const userJson = await AsyncStorage.getItem(USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }

  // Food Logging Methods
  async logFoodText(note: string): Promise<TextLogResponse> {
    const normalized = note.trim().toLowerCase();
    const cached = this.textEstimateCache.get(normalized);
    const now = Date.now();

    if (cached && now - cached.ts < 5 * 60 * 1000) {
      return cached.data;
    }

    const response = await this.api.post<TextLogResponse>(
      API_CONFIG.ENDPOINTS.AI.LOG_TEXT,
      { note }
    );

    this.textEstimateCache.set(normalized, { data: response.data, ts: now });
    if (this.textEstimateCache.size > 30) {
      const oldestKey = this.textEstimateCache.keys().next().value;
      if (oldestKey) {
        this.textEstimateCache.delete(oldestKey);
      }
    }

    return response.data;
  }

  // Meal History Methods
  async saveMealHistory(payload: SaveMealHistoryInput): Promise<MealHistory> {
    const requestBody =
      typeof payload === 'string'
        ? { note: payload }
        : { note: payload.note };

    const response = await this.api.post<MealHistory>(
      API_CONFIG.ENDPOINTS.USER.MEAL_HISTORY,
      requestBody
    );
    return response.data;
  }

  async getMealHistory(limit = 30): Promise<MealHistory[]> {
    const response = await this.api.get<MealHistory[]>(API_CONFIG.ENDPOINTS.USER.MEAL_HISTORY, {
      params: { limit },
    });
    return response.data;
  }

  async updateMealHistory(mealId: number, note: string): Promise<MealHistory> {
    const response = await this.api.put<MealHistory>(
      `${API_CONFIG.ENDPOINTS.USER.MEAL_HISTORY}/${mealId}`,
      { note }
    );
    return response.data;
  }

  // User Methods
  async getUserProfile(): Promise<User> {
    const response = await this.api.get<User>(API_CONFIG.ENDPOINTS.USER.PROFILE);
    return response.data;
  }
}

export default new ApiService();
