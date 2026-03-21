# CalAI Mobile App

A React Native mobile app for intelligent meal logging and calorie tracking with MyFitnessPal-style UI.

## Features

- **Smart Food Logging**: Log meals by typing descriptions (e.g., "250g beef with 1 cup rice")
- **AI-Powered Recognition**: Automatically parses foods, portions, and estimates calories
- **Portion Adjustment**: Fine-tune calorie estimates with quick adjustment buttons (½, 1.5x, 2x)
- **Daily Dashboard**: Track daily calorie intake with progress bars and summaries
- **Meal History**: View all logged meals with timestamps
- **User Authentication**: Secure login and registration

## Tech Stack

- **React Native** with **Expo** for cross-platform development
- **React Navigation** for tab-based navigation
- **React Native Paper** for Material Design UI
- **Axios** for API communication
- **AsyncStorage** for local token persistence

## Setup Instructions

### Prerequisites
- Node.js 18+
- npm or yarn
- Expo CLI: `npm install -g expo-cli`

### Installation

1. Install dependencies:
```bash
npm install
```

2. Create `.env` file with API configuration (backend URL):
```bash
# Update API_CONFIG.BASE_URL in constants/config.ts for your backend
# Default: http://localhost:8000
```

3. Start the development server:
```bash
npm start
```

4. Run on your device or emulator:
- **iOS**: `npm run ios` (requires macOS)
- **Android**: `npm run android` (requires Android SDK)
- **Web**: `npm run web`
- **Expo Go**: Scan the QR code with [Expo Go](https://expo.dev/client)

## App Structure

```
screens/
  ├── AuthScreen.tsx        # Login/Registration UI
  ├── HomeScreen.tsx        # Daily dashboard with calorie summary
  └── LogFoodScreen.tsx     # Main meal logging interface

services/
  └── api.ts               # Backend API integration with axios

context/
  └── AuthContext.tsx      # Auth state management and persistence

navigation/
  └── AppNavigator.tsx     # Tab-based navigation for authenticated users

types/
  └── index.ts             # TypeScript interfaces matching backend

constants/
  └── config.ts            # API endpoints and configuration
```

## Usage

### Logging a Meal

1. Tap the "Log Meal" tab
2. Enter your meal description (e.g., "2 tacos and fries" or "250g salmon with rice")
3. App automatically parses foods and estimates calories
4. Adjust portions using quick buttons (½, 1.5x, 2x) if needed
5. Tap "Save to History" to log the meal

### Supported Format Examples

- **Explicit weights**: "250g beef", "8oz steak", "1lb chicken"
- **Multi-item meals**: "Hamburger and fries", "Chicken with 1 cup rice"
- **Quantities**: "2 tacos", "half pizza", "1 cup pasta"
- **Typos**: App automatically corrects common misspellings

## Backend Integration

This app communicates with the CalAI backend API. Ensure the backend is running.

### Required Endpoints

- `POST /auth/register` - User registration
- `POST /auth/login` - User login  
- `POST /ai/log-text` - Log food by text description
- `GET /user/meal-history` - Retrieve meal history
- `GET /user/me` - Get user profile

## Building for Production

```bash
# For iOS
eas build --platform ios

# For Android
eas build --platform android
```

Requires EAS CLI: https://expo.dev

## Troubleshooting

### Module not found errors
```bash
npm install
npm start -- --clear
```

### API connection issues
- Verify backend is running on `http://localhost:8000`
- Update `API_CONFIG.BASE_URL` in `constants/config.ts`
- Check network/firewall permissions
