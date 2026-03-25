import React from 'react';
import { View, Text, ScrollView, StyleSheet, Pressable } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import apiService from '@/services/api';
import { useFocusEffect } from '@react-navigation/native';
import { Calendar, Pencil } from 'lucide-react-native';
import { MealHistory } from '@/types';
import { paperTheme } from '@/constants/paperTheme';

export default function LogsScreen() {
  const [history, setHistory] = React.useState<MealHistory[]>([]);

  useFocusEffect(
    React.useCallback(() => {
      loadHistory();
    }, [])
  );

  const loadHistory = async () => {
    try {
      const response = await apiService.getMealHistory(30);
      setHistory(response);
    } catch {
      setHistory([]);
    }
  };

  const getMealName = (meal: MealHistory) => {
    const parsed = meal.items?.[0]?.parsed_food?.trim();
    if (parsed) {
      return parsed.charAt(0).toUpperCase() + parsed.slice(1);
    }

    const note = (meal.note || 'Meal').toString().trim();
    const cleaned = note.replace(/^\s*\d+\s*(g|grams?)?\s*/i, '').trim();
    const base = cleaned || note;
    return base.charAt(0).toUpperCase() + base.slice(1);
  };

  const getMealDose = (meal: MealHistory) => {
    const gramsFromItems = (meal.items || []).reduce(
      (sum, item) => sum + (item.estimated_grams || 0),
      0
    );
    if (gramsFromItems > 0) {
      return `${Math.round(gramsFromItems)}g`;
    }

    const note = (meal.note || '').toString();
    const match = note.match(/(\d+)\s*(g|grams?)/i);
    return match ? `${match[1]}g` : '';
  };

  return (
    <SafeAreaView style={styles.root} edges={['top', 'left', 'right']}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.headRow}>
          <View>
            <Text style={styles.title}>Food Logs</Text>
            <Text style={styles.subtitle}>Capture what you eat, instantly</Text>
          </View>
          <View style={styles.datePill}>
            <Calendar size={14} color={paperTheme.colors.onSurfaceVariant} />
            <Text style={styles.dateTxt}>Today</Text>
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Meal History</Text>
          {history.length === 0 ? (
            <Text style={styles.tip}>No saved meals yet. Tap + to log your first one.</Text>
          ) : (
            history.map((meal, index) => (
              <View key={meal.id} style={[styles.historyRow, index === history.length - 1 && styles.historyRowLast]}>
                <View style={styles.historyTextWrap}>
                  <Text style={styles.historyNote} numberOfLines={2}>{getMealName(meal)}</Text>
                  {!!getMealDose(meal) && <Text style={styles.historyGrams}>{getMealDose(meal)}</Text>}
                </View>
                <View style={styles.historyRight}>
                  <View style={styles.calorieEditRow}>
                    <Text style={styles.historyCalories}>{Math.round(meal.total_calories)} kcal</Text>
                    <Pressable style={styles.editIconBtn}>
                      <Pencil size={15} color={paperTheme.colors.onSurfaceVariant} />
                    </Pressable>
                  </View>
                  <Text style={styles.historyMeta}>
                    {new Date(meal.timestamp).toLocaleString([], {
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </Text>
                </View>
              </View>
            ))
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: paperTheme.colors.background },
  content: { padding: 20, paddingBottom: 130 },
  headRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 14 },
  title: { color: paperTheme.colors.onBackground, fontSize: 24, lineHeight: 34, fontWeight: '600' },
  subtitle: { color: paperTheme.colors.onSurfaceVariant, fontSize: 13, fontWeight: '500', marginTop: 2 },
  datePill: { flexDirection: 'row', gap: 6, alignItems: 'center', backgroundColor: paperTheme.colors.surfaceVariant, borderColor: paperTheme.colors.outline, borderWidth: 1, paddingHorizontal: 10, paddingVertical: 6, borderRadius: 999, marginTop: 4 },
  dateTxt: { color: paperTheme.colors.onSurfaceVariant, fontSize: 12, fontWeight: '600' },
  card: { backgroundColor: 'transparent', padding: 0, marginBottom: 12 },
  cardTitle: { color: paperTheme.colors.onSurface, fontSize: 20, lineHeight: 24, fontWeight: '600', marginBottom: 12 },
  tip: { color: paperTheme.colors.onSurfaceVariant, fontSize: 12 },
  historyRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', paddingVertical: 14, borderTopWidth: 1, borderTopColor: paperTheme.colors.outline },
  historyRowLast: { borderBottomWidth: 1, borderBottomColor: paperTheme.colors.outline },
  historyTextWrap: { flex: 1, marginRight: 10 },
  historyNote: { color: paperTheme.colors.onSurface, fontSize: 20, lineHeight: 24, fontWeight: '600' },
  historyGrams: { color: '#8E8E8E', fontSize: 13, marginTop: 4, fontWeight: '500' },
  historyMeta: { color: paperTheme.colors.onSurfaceVariant, fontSize: 12, marginTop: 6 },
  historyRight: { alignItems: 'flex-end', minWidth: 150 },
  calorieEditRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  historyCalories: { color: paperTheme.colors.primary, fontWeight: '800', fontSize: 15 },
  editIconBtn: {
    width: 30,
    height: 30,
    borderRadius: 9,
    borderWidth: 1,
    borderColor: '#4A4A4A',
    backgroundColor: '#171717',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
