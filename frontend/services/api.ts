import axios, { AxiosInstance, AxiosError } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_CONFIG } from '../constants/config';
import { TextLogResponse, AuthResponse, User, MealHistory } from '../types';

class ApiService {
  private api: AxiosInstance;
  private token: string | null = null;

  private async persistAuth(auth: AuthResponse): Promise<void> {
    this.token = auth.access_token;
    await AsyncStorage.setItem('authToken', this.token);
    await AsyncStorage.setItem('user', JSON.stringify(auth.user));
  }

  constructor() {
    this.api = axios.create({
      baseURL: API_CONFIG.BASE_URL,
      timeout: 10000,
    });

    // Add request interceptor to include auth token
    this.api.interceptors.request.use(
      async (config) => {
        const token = await AsyncStorage.getItem('authToken');
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
        if (error.response?.status === 401) {
          // Token expired, clear storage and redirect to login
          await AsyncStorage.removeItem('authToken');
          await AsyncStorage.removeItem('user');
          // This should trigger auth context to reset
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
    this.token = null;
    await AsyncStorage.removeItem('authToken');
    await AsyncStorage.removeItem('user');
  }

  async getStoredToken(): Promise<string | null> {
    return AsyncStorage.getItem('authToken');
  }

  async getStoredUser(): Promise<User | null> {
    const userJson = await AsyncStorage.getItem('user');
    return userJson ? JSON.parse(userJson) : null;
  }

  // Food Logging Methods
  async logFoodText(note: string): Promise<TextLogResponse> {
    const response = await this.api.post<TextLogResponse>(
      API_CONFIG.ENDPOINTS.AI.LOG_TEXT,
      { note }
    );
    return response.data;
  }

  // Meal History Methods
  async saveMealHistory(payload: TextLogResponse): Promise<MealHistory> {
    const response = await this.api.post<MealHistory>(
      API_CONFIG.ENDPOINTS.USER.MEAL_HISTORY,
      payload
    );
    return response.data;
  }

  async getMealHistory(limit = 30): Promise<MealHistory[]> {
    const response = await this.api.get<MealHistory[]>(API_CONFIG.ENDPOINTS.USER.MEAL_HISTORY, {
      params: { limit },
    });
    return response.data;
  }

  // User Methods
  async getUserProfile(): Promise<User> {
    const response = await this.api.get<User>(API_CONFIG.ENDPOINTS.USER.PROFILE);
    return response.data;
  }
}

export default new ApiService();
