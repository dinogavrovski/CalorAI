// API Response Types
export interface TextLogItem {
  note_part: string;
  parsed_food: string;
  quantity: number;
  unit: string;
  estimated_grams: number;
  kcal_per_gram: number;
  calories: number;
  calorie_range: [number, number];
  nutrition_source: string;
  matched_description: string;
}

export interface TextLogResponse {
  note: string;
  items: TextLogItem[];
  total_calories: number;
  total_calorie_range: [number, number];
}

export interface User {
  id: string;
  username: string;
  email: string;
}

export interface AuthResponse {
  access_token: string;
  token_type: string;
  user: User;
}

export interface MealHistory {
  id: string;
  user_id: string;
  note: string;
  items: TextLogItem[];
  total_calories: number;
  actual_calories?: number;
  timestamp: string;
}

// UI State Types
export interface FoodLogUIState extends TextLogResponse {
  isLoading: boolean;
  error: string | null;
}
