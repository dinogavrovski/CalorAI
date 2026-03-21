// Backend API Configuration
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8000', // Update for production
  ENDPOINTS: {
    AUTH: {
      REGISTER: '/auth/register',
      LOGIN: '/auth/login',
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
