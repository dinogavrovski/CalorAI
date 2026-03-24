import React, { useState } from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Plus, Zap, Flame } from 'lucide-react-native';
import { useFocusEffect } from '@react-navigation/native';
import { useRouter } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import BottomSheetMealLogger from '@/components/BottomSheetMealLogger';
import apiService from '@/services/api';
import { colors } from '@/constants/uiTheme';

export default function HomeScreen() {
  const router = useRouter();
  const { user } = useAuth();
  const [dailyCalories, setDailyCalories] = useState(0);
  const [recentMeals, setRecentMeals] = useState<any[]>([]);
  const [mealLoggerVisible, setMealLoggerVisible] = useState(false);
  const goalCalories = 2200;

  const progressPct = Math.min((dailyCalories / goalCalories) * 100, 100);
  const remainingCalories = Math.max(0, goalCalories - dailyCalories);

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
          <View style={styles.streakBadge}>
            <Zap size={16} color={colors.primaryDark} />
            <Text style={styles.streakText}>12 day streak</Text>
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Daily Fuel</Text>
          <View style={styles.calWrap}>
            <Flame size={22} color={colors.primary} />
            <Text style={styles.calNumber}>{remainingCalories}</Text>
            <Text style={styles.muted}>remaining calories</Text>
          </View>
          <View style={styles.progressTrack}>
            <View style={[styles.progressFill, { width: `${progressPct}%` }]} />
          </View>
          <Text style={styles.smallMuted}>{Math.round(dailyCalories)} / {goalCalories} kcal</Text>

          <View style={styles.metricsRow}>
            <View style={styles.metricCard}>
              <Text style={styles.metricLabel}>Meals</Text>
              <Text style={styles.metricValue}>{recentMeals.length}</Text>
            </View>
            <View style={styles.metricCard}>
              <Text style={styles.metricLabel}>Consistency</Text>
              <Text style={styles.metricValue}>{Math.round(progressPct)}%</Text>
            </View>
          </View>
        </View>

        <View style={styles.actionRow}>
          <Pressable style={styles.actionPrimary} onPress={() => setMealLoggerVisible(true)}>
            <Plus size={18} color="#fff" />
            <Text style={styles.actionPrimaryText}>Log Meal</Text>
          </Pressable>
          <Pressable style={styles.actionGhost} onPress={() => router.push('/(tabs)/progress')}>
            <Text style={styles.actionGhostText}>Progress</Text>
          </Pressable>
        </View>

        <Text style={styles.sectionTitle}>Today’s Meals</Text>
        {recentMeals.length === 0 ? (
          <View style={styles.emptyCard}><Text style={styles.muted}>No meals logged yet</Text></View>
        ) : (
          recentMeals.slice(0, 6).map((meal, index) => (
            <View key={meal.id || index} style={styles.mealCard}>
              <View>
                <Text style={styles.mealName}>{meal.note || 'Meal'}</Text>
                <Text style={styles.smallMuted}>{meal.timestamp ? new Date(meal.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Today'}</Text>
              </View>
              <Text style={styles.mealCal}>{Math.round(meal.total_calories || 0)} kcal</Text>
            </View>
          ))
        )}
      </ScrollView>

      <BottomSheetMealLogger
        isVisible={mealLoggerVisible}
        onClose={() => setMealLoggerVisible(false)}
        onSuccess={loadMealHistory}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: colors.bg },
  content: { padding: 20, paddingBottom: 100 },
  headerRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 },
  title: { fontSize: 30, fontWeight: '900', color: colors.text, letterSpacing: -0.5 },
  muted: { color: colors.muted, fontSize: 13 },
  smallMuted: { color: colors.muted, fontSize: 12, marginTop: 6 },
  streakBadge: { flexDirection: 'row', alignItems: 'center', gap: 6, backgroundColor: colors.chip, borderRadius: 999, paddingVertical: 7, paddingHorizontal: 12, borderWidth: 1, borderColor: '#F3CEB9' },
  streakText: { color: colors.primaryDark, fontSize: 12, fontWeight: '700' },
  card: { backgroundColor: colors.card, borderColor: colors.border, borderWidth: 1, borderRadius: 20, padding: 18, marginBottom: 14, shadowColor: '#A89273', shadowOpacity: 0.12, shadowRadius: 18, shadowOffset: { width: 0, height: 8 }, elevation: 3 },
  cardTitle: { fontSize: 18, fontWeight: '700', color: colors.text, marginBottom: 14 },
  calWrap: { alignItems: 'center', marginBottom: 10 },
  calNumber: { fontSize: 36, fontWeight: '900', color: colors.text, marginTop: 4 },
  progressTrack: { height: 9, borderRadius: 99, backgroundColor: '#F1EBE1', overflow: 'hidden', marginTop: 8 },
  progressFill: { height: 8, borderRadius: 99, backgroundColor: colors.primary },
  metricsRow: { flexDirection: 'row', gap: 10, marginTop: 14 },
  metricCard: { flex: 1, borderRadius: 12, backgroundColor: colors.soft, borderWidth: 1, borderColor: '#F0DDC8', paddingVertical: 10, paddingHorizontal: 12 },
  metricLabel: { color: colors.muted, fontSize: 11, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 0.5 },
  metricValue: { color: colors.text, fontSize: 18, fontWeight: '800', marginTop: 2 },
  actionRow: { flexDirection: 'row', gap: 10, marginBottom: 18 },
  actionPrimary: { flex: 1, height: 50, borderRadius: 14, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center', flexDirection: 'row', gap: 8 },
  actionPrimaryText: { color: '#fff', fontWeight: '700' },
  actionGhost: { width: 116, height: 50, borderRadius: 14, borderWidth: 1, borderColor: '#D8CEBE', alignItems: 'center', justifyContent: 'center', backgroundColor: '#FFF7EF' },
  actionGhostText: { color: colors.text, fontWeight: '700', fontSize: 13 },
  sectionTitle: { fontSize: 18, fontWeight: '800', color: colors.text, marginBottom: 10 },
  emptyCard: { backgroundColor: colors.card, borderColor: colors.border, borderWidth: 1, borderRadius: 12, padding: 18, alignItems: 'center' },
  mealCard: { backgroundColor: colors.card, borderColor: colors.border, borderWidth: 1, borderRadius: 14, padding: 14, marginBottom: 8, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  mealName: { color: colors.text, fontWeight: '700' },
  mealCal: { color: colors.secondary, fontWeight: '800' },
});
