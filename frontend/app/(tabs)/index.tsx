import React, { useState } from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Plus, Zap, Flame } from 'lucide-react-native';
import { useFocusEffect } from '@react-navigation/native';
import { useAuth } from '@/context/AuthContext';
import apiService from '@/services/api';
import { colors } from '@/constants/uiTheme';

export default function HomeScreen() {
  const { user } = useAuth();
  const [dailyCalories, setDailyCalories] = useState(0);
  const [recentMeals, setRecentMeals] = useState<any[]>([]);

  useFocusEffect(
    React.useCallback(() => {
      loadMealHistory();
    }, [])
  );

  const loadMealHistory = async () => {
    try {
      const meals = await apiService.getMealHistory();
      setRecentMeals(meals);
      const total = meals.reduce((acc: number, meal: any) => acc + (meal.total_calories || 0), 0);
      setDailyCalories(total);
    } catch {
      setRecentMeals([]);
      setDailyCalories(0);
    }
  };

  return (
    <SafeAreaView style={styles.root} edges={['top', 'left', 'right']}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.headerRow}>
          <View>
            <Text style={styles.muted}>Welcome back,</Text>
            <Text style={styles.title}>{user?.username || 'User'}</Text>
          </View>
          <View style={styles.zapWrap}>
            <Zap size={18} color={colors.primary} />
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Daily Summary</Text>
          <View style={styles.calWrap}>
            <Flame size={22} color={colors.primary} />
            <Text style={styles.calNumber}>{Math.max(0, 2200 - dailyCalories)}</Text>
            <Text style={styles.muted}>remaining calories</Text>
          </View>
          <View style={styles.progressTrack}>
            <View style={[styles.progressFill, { width: `${Math.min((dailyCalories / 2200) * 100, 100)}%` }]} />
          </View>
          <Text style={styles.smallMuted}>{dailyCalories} / 2200 kcal</Text>
        </View>

        <View style={styles.actionRow}>
          <Pressable style={styles.actionPrimary}>
            <Plus size={18} color="#fff" />
            <Text style={styles.actionPrimaryText}>Log Meal</Text>
          </Pressable>
          <Pressable style={styles.actionGhost}>
            <Text style={styles.actionGhostText}>Quick Cal</Text>
          </Pressable>
        </View>

        <Text style={styles.sectionTitle}>Today’s Meals</Text>
        {recentMeals.length === 0 ? (
          <View style={styles.emptyCard}><Text style={styles.muted}>No meals logged yet</Text></View>
        ) : (
          recentMeals.slice(0, 6).map((meal, index) => (
            <View key={index} style={styles.mealCard}>
              <View>
                <Text style={styles.mealName}>{meal.note || 'Meal'}</Text>
                <Text style={styles.smallMuted}>{meal.timestamp ? new Date(meal.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Today'}</Text>
              </View>
              <Text style={styles.mealCal}>{Math.round(meal.total_calories || 0)} kcal</Text>
            </View>
          ))
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: colors.bg },
  content: { padding: 20, paddingBottom: 100 },
  headerRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 },
  title: { fontSize: 28, fontWeight: '800', color: colors.text },
  muted: { color: colors.muted, fontSize: 13 },
  smallMuted: { color: colors.muted, fontSize: 12, marginTop: 6 },
  zapWrap: { width: 38, height: 38, borderRadius: 19, backgroundColor: colors.chip, alignItems: 'center', justifyContent: 'center' },
  card: { backgroundColor: colors.card, borderColor: colors.border, borderWidth: 1, borderRadius: 18, padding: 18, marginBottom: 14 },
  cardTitle: { fontSize: 18, fontWeight: '700', color: colors.text, marginBottom: 14 },
  calWrap: { alignItems: 'center', marginBottom: 10 },
  calNumber: { fontSize: 36, fontWeight: '900', color: colors.text, marginTop: 4 },
  progressTrack: { height: 8, borderRadius: 99, backgroundColor: '#E5E7EB', overflow: 'hidden', marginTop: 8 },
  progressFill: { height: 8, borderRadius: 99, backgroundColor: colors.primary },
  actionRow: { flexDirection: 'row', gap: 10, marginBottom: 18 },
  actionPrimary: { flex: 1, height: 48, borderRadius: 12, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center', flexDirection: 'row', gap: 8 },
  actionPrimaryText: { color: '#fff', fontWeight: '700' },
  actionGhost: { width: 110, height: 48, borderRadius: 12, borderWidth: 1, borderColor: colors.border, alignItems: 'center', justifyContent: 'center', backgroundColor: '#fff' },
  actionGhostText: { color: colors.text, fontWeight: '700' },
  sectionTitle: { fontSize: 18, fontWeight: '800', color: colors.text, marginBottom: 10 },
  emptyCard: { backgroundColor: colors.card, borderColor: colors.border, borderWidth: 1, borderRadius: 12, padding: 18, alignItems: 'center' },
  mealCard: { backgroundColor: colors.card, borderColor: colors.border, borderWidth: 1, borderRadius: 12, padding: 14, marginBottom: 8, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  mealName: { color: colors.text, fontWeight: '700' },
  mealCal: { color: colors.text, fontWeight: '700' },
});
