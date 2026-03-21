import React, { useState } from 'react';
import { useFocusEffect } from '@react-navigation/native';
import {
  View,
  ScrollView,
  StyleSheet,
  RefreshControl,
} from 'react-native';
import {
  Card,
  Text,
  ProgressBar,
  Surface,
  Button,
  IconButton,
  Avatar,
} from 'react-native-paper';
import { useAuth } from '../context/AuthContext';
import apiService from '../services/api';

export const HomeScreen = () => {
  const { user, logout } = useAuth();
  const [meals, setMeals] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  useFocusEffect(
    React.useCallback(() => {
      loadMealHistory();
    }, [])
  );

  const loadMealHistory = async () => {
    setLoading(true);
    try {
      const history = await apiService.getMealHistory();
      setMeals(history);
    } catch (error) {
      console.error('Failed to load meal history:', error);
    } finally {
      setLoading(false);
    }
  };

  const getTodayTotal = () => {
    const today = new Date().toDateString();
    return meals
      .filter((meal) => new Date(meal.timestamp).toDateString() === today)
      .reduce((sum, meal) => sum + meal.total_calories, 0);
  };

  const getDailyGoal = 2000;
  const todayTotal = getTodayTotal();
  const remaining = Math.max(0, getDailyGoal - todayTotal);
  const progress = Math.min(todayTotal / getDailyGoal, 1);

  return (
    <ScrollView
      style={styles.container}
      refreshControl={<RefreshControl refreshing={loading} onRefresh={loadMealHistory} />}
    >
      {/* Header with User Info */}
      <Surface style={styles.headerCard}>
        <View style={styles.headerContent}>
          <Avatar.Text size={48} label={user?.username?.[0]?.toUpperCase() || 'U'} />
          <View style={styles.userInfo}>
            <Text style={styles.greeting}>Welcome, {user?.username}!</Text>
            <Text style={styles.date}>{new Date().toLocaleDateString()}</Text>
          </View>
          <IconButton
            icon="logout"
            size={20}
            onPress={logout}
            style={styles.logoutButton}
          />
        </View>
      </Surface>

      {/* Daily Summary Card */}
      <Card style={styles.summaryCard}>
        <Card.Content>
          <Text style={styles.cardTitle}>Daily Calorie Summary</Text>

          {/* Large Calorie Display */}
          <View style={styles.calorieDisplay}>
            <View style={styles.calorieColumn}>
              <Text style={styles.calorieLabel}>Consumed</Text>
              <Text style={styles.calorieNumber}>{Math.round(todayTotal)}</Text>
            </View>
            <View style={styles.calorieColumn}>
              <Text style={styles.calorieLabel}>Remaining</Text>
              <Text
                style={[
                  styles.calorieNumber,
                  remaining < 0 && styles.calorieNumberRed,
                ]}
              >
                {Math.round(remaining)}
              </Text>
            </View>
            <View style={styles.calorieColumn}>
              <Text style={styles.calorieLabel}>Daily Goal</Text>
              <Text style={styles.calorieNumber}>{getDailyGoal}</Text>
            </View>
          </View>

          {/* Progress Bar */}
          <View style={styles.progressSection}>
            <ProgressBar progress={progress} style={styles.progressBar} />
            <Text style={styles.progressText}>
              {Math.round(progress * 100)}% of daily goal
            </Text>
          </View>
        </Card.Content>
      </Card>

      {/* Macros Breakdown (Placeholder) */}
      <Card style={styles.macrosCard}>
        <Card.Content>
          <Text style={styles.cardTitle}>Macros Breakdown</Text>
          <View style={styles.macroRow}>
            <View style={styles.macroItem}>
              <Text style={styles.macroLabel}>Protein</Text>
              <Text style={styles.macroValue}>0g</Text>
            </View>
            <View style={styles.macroItem}>
              <Text style={styles.macroLabel}>Carbs</Text>
              <Text style={styles.macroValue}>0g</Text>
            </View>
            <View style={styles.macroItem}>
              <Text style={styles.macroLabel}>Fat</Text>
              <Text style={styles.macroValue}>0g</Text>
            </View>
          </View>
          <Text style={styles.macroNote}>
            Track macros by adding detailed food entries
          </Text>
        </Card.Content>
      </Card>

      {/* Today's Meals */}
      <Card style={styles.mealsCard}>
        <Card.Content>
          <Text style={styles.cardTitle}>Today's Meals</Text>

          {meals.length === 0 ? (
            <Text style={styles.emptyText}>No meals logged yet. Start tracking!</Text>
          ) : (
            meals.map((meal, idx) => (
              <View key={idx} style={styles.mealItem}>
                <View style={styles.mealInfo}>
                  <Text style={styles.mealNote} numberOfLines={2}>
                    {meal.note}
                  </Text>
                  <Text style={styles.mealTime}>
                    {new Date(meal.timestamp).toLocaleTimeString([], {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </Text>
                </View>
                <Text style={styles.mealCalories}>{Math.round(meal.total_calories)}</Text>
              </View>
            ))
          )}
        </Card.Content>
      </Card>

      {/* Quick Links */}
      <View style={styles.quickLinks}>
        <Button mode="outlined" onPress={() => {}}>
          View Reports
        </Button>
        <Button mode="outlined" onPress={() => {}}>
          Settings
        </Button>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  headerCard: {
    padding: 16,
    borderRadius: 12,
    marginBottom: 16,
    elevation: 2,
  },
  headerContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  userInfo: {
    flex: 1,
    marginLeft: 12,
  },
  greeting: {
    fontSize: 18,
    fontWeight: '700',
  },
  date: {
    fontSize: 12,
    color: '#999',
    marginTop: 4,
  },
  logoutButton: {
    margin: 0,
  },
  summaryCard: {
    marginBottom: 16,
    borderRadius: 12,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 12,
  },
  calorieDisplay: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingVertical: 16,
    backgroundColor: '#f9f9f9',
    borderRadius: 8,
    marginBottom: 12,
  },
  calorieColumn: {
    alignItems: 'center',
  },
  calorieLabel: {
    fontSize: 11,
    color: '#999',
    marginBottom: 4,
  },
  calorieNumber: {
    fontSize: 24,
    fontWeight: '700',
    color: '#0066cc',
  },
  calorieNumberRed: {
    color: '#ff6666',
  },
  progressSection: {
    marginTop: 8,
  },
  progressBar: {
    height: 12,
    borderRadius: 6,
    marginBottom: 8,
  },
  progressText: {
    fontSize: 12,
    color: '#666',
    textAlign: 'center',
  },
  macrosCard: {
    marginBottom: 16,
    borderRadius: 12,
  },
  macroRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingVertical: 12,
    backgroundColor: '#f9f9f9',
    borderRadius: 8,
    marginBottom: 8,
  },
  macroItem: {
    alignItems: 'center',
  },
  macroLabel: {
    fontSize: 11,
    color: '#999',
    marginBottom: 4,
  },
  macroValue: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
  },
  macroNote: {
    fontSize: 11,
    color: '#bbb',
    textAlign: 'center',
    fontStyle: 'italic',
  },
  mealsCard: {
    marginBottom: 16,
    borderRadius: 12,
  },
  mealItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  mealInfo: {
    flex: 1,
    marginRight: 12,
  },
  mealNote: {
    fontSize: 13,
    fontWeight: '500',
  },
  mealTime: {
    fontSize: 11,
    color: '#999',
    marginTop: 4,
  },
  mealCalories: {
    fontSize: 14,
    fontWeight: '700',
    color: '#0066cc',
  },
  emptyText: {
    fontSize: 13,
    color: '#999',
    textAlign: 'center',
    paddingVertical: 20,
    fontStyle: 'italic',
  },
  quickLinks: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingVertical: 16,
    marginBottom: 20,
  },
});
