// Backend API Configuration
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8000', // Update for production
  GOOGLE: {
    WEB_CLIENT_ID: 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com',
    ANDROID_CLIENT_ID: 'YOUR_ANDROID_CLIENT_ID.apps.googleusercontent.com',
    EXPO_CLIENT_ID: 'YOUR_EXPO_CLIENT_ID.apps.googleusercontent.com',
  },
  ENDPOINTS: {
    AUTH: {
      REGISTER: '/auth/register',
      LOGIN: '/auth/login',
      GOOGLE: '/auth/google',
      REFRESH: '/auth/refresh',
    },
    AI: {
      LOG_TEXT: '/ai/log-text',
      ANALYZE_MEAL: '/ai/analyze-meal',
    },
    USER: {
      PROFILE: '/user/me',
      MEAL_HISTORY: '/user/meal-history',
    },
  },
};
