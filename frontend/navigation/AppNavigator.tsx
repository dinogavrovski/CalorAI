import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { HomeScreen } from '../screens/HomeScreen';
import { LogFoodScreen } from '../screens/LogFoodScreen';

const Tab = createBottomTabNavigator();

export const AppNavigator = () => {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof MaterialCommunityIcons.glyphMap = 'home';

          if (route.name === 'Home') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'LogFood') {
            iconName = focused ? 'plus-circle' : 'plus-circle-outline';
          }

          return (
            <MaterialCommunityIcons name={iconName} size={size} color={color} />
          );
        },
        tabBarActiveTintColor: '#0066cc',
        tabBarInactiveTintColor: '#999',
        headerShown: true,
        headerTitleStyle: {
          fontWeight: '700',
        },
        tabBarStyle: {
          borderTopWidth: 1,
          borderTopColor: '#eee',
        },
      })}
    >
      <Tab.Screen
        name="Home"
        component={HomeScreen}
        options={{
          title: 'Dashboard',
          headerTitleAlign: 'left',
        }}
      />
      <Tab.Screen
        name="LogFood"
        component={LogFoodScreen}
        options={{
          title: 'Log Meal',
          headerTitleAlign: 'left',
        }}
      />
    </Tab.Navigator>
  );
};
