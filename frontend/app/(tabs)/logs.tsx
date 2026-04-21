import React from 'react';
import { View, Text, ScrollView, StyleSheet, Pressable } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import apiService from '@/services/api';
import { useFocusEffect } from '@react-navigation/native';
import { Pencil } from 'lucide-react-native';
import { MealHistory } from '@/types';
import { paperTheme } from '@/constants/paperTheme';
import BottomSheetMealEditor from '@/components/BottomSheetMealEditor';

export default function LogsScreen() {
  const [history, setHistory] = React.useState<MealHistory[]>([]);
  const [editingMeal, setEditingMeal] = React.useState<MealHistory | null>(null);

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
    const note = (meal.note || 'Meal').toString().trim();
    if (note) {
      return note.charAt(0).toUpperCase() + note.slice(1);
    }

    const parsedItems = (meal.items || [])
      .map((item) => item.parsed_food?.trim())
      .filter(Boolean)
      .slice(0, 3)
      .join(', ');

    if (parsedItems) {
      return parsedItems.charAt(0).toUpperCase() + parsedItems.slice(1);
    }

    return 'Meal';
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
                    <Pressable style={styles.editIconBtn} onPress={() => setEditingMeal(meal)}>
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

      <BottomSheetMealEditor
        isVisible={!!editingMeal}
        meal={editingMeal}
        onClose={() => setEditingMeal(null)}
        onSaved={() => {
          setEditingMeal(null);
          loadHistory();
        }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: paperTheme.colors.background },
  content: { padding: 20, paddingBottom: 130 },
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
