import React, { useState } from 'react';
import { View, Text, ScrollView, Pressable, TextInput, Alert, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import apiService from '@/services/api';
import { useFocusEffect } from '@react-navigation/native';
import { Send, Calendar } from 'lucide-react-native';
import { MealHistory } from '@/types';
import { colors } from '@/constants/uiTheme';

export default function LogsScreen() {
  const [mealInput, setMealInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [history, setHistory] = useState<MealHistory[]>([]);

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

  const handleLogMeal = async () => {
    if (!mealInput.trim()) {
      Alert.alert('Error', 'Please enter a meal description');
      return;
    }

    setIsLoading(true);
    try {
      const estimate = await apiService.logFoodText(mealInput);
      const savedMeal = await apiService.saveMealHistory(estimate);
      setHistory((prev) => [savedMeal, ...prev].slice(0, 30));
      Alert.alert('Success', `Saved ${Math.round(savedMeal.total_calories)} kcal`);
      setMealInput('');
    } catch {
      Alert.alert('Error', 'Could not log meal');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.root} edges={['top', 'left', 'right']}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.headRow}>
          <Text style={styles.title}>Food Logs</Text>
          <View style={styles.datePill}>
            <Calendar size={14} color={colors.muted} />
            <Text style={styles.dateTxt}>Today</Text>
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Log a meal</Text>
          <View style={styles.inputRow}>
            <TextInput
              style={styles.input}
              placeholder="e.g. chicken bowl with rice"
              placeholderTextColor="#94A3B8"
              value={mealInput}
              onChangeText={setMealInput}
              editable={!isLoading}
            />
            <Pressable style={styles.sendBtn} onPress={handleLogMeal} disabled={isLoading}>
              <Send size={18} color="#fff" />
            </Pressable>
          </View>
          <Text style={styles.tip}>Tip: include portion sizes for better estimates.</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Meal History</Text>
          {history.length === 0 ? (
            <Text style={styles.tip}>No saved meals yet.</Text>
          ) : (
            history.map((meal) => (
              <View key={meal.id} style={styles.historyRow}>
                <View style={styles.historyTextWrap}>
                  <Text style={styles.historyNote} numberOfLines={2}>{meal.note}</Text>
                  <Text style={styles.historyMeta}>
                    {new Date(meal.timestamp).toLocaleString([], {
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </Text>
                </View>
                <View style={styles.historyRight}>
                  <Text style={styles.historyCalories}>{Math.round(meal.total_calories)} kcal</Text>
                  <Text style={styles.historyRange}>
                    {Math.round(meal.total_calorie_range[0])}-{Math.round(meal.total_calorie_range[1])}
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
  root: { flex: 1, backgroundColor: colors.bg },
  content: { padding: 20, paddingBottom: 100 },
  headRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 },
  title: { color: colors.text, fontSize: 28, fontWeight: '800' },
  datePill: { flexDirection: 'row', gap: 6, alignItems: 'center', backgroundColor: '#fff', borderColor: colors.border, borderWidth: 1, paddingHorizontal: 10, paddingVertical: 6, borderRadius: 999 },
  dateTxt: { color: colors.muted, fontSize: 12, fontWeight: '600' },
  card: { backgroundColor: '#fff', borderColor: colors.border, borderWidth: 1, borderRadius: 16, padding: 16 },
  cardTitle: { color: colors.text, fontSize: 17, fontWeight: '700', marginBottom: 12 },
  inputRow: { flexDirection: 'row', gap: 10 },
  input: { flex: 1, backgroundColor: '#F8FAFC', borderRadius: 12, borderWidth: 1, borderColor: colors.border, paddingHorizontal: 12, height: 46, color: colors.text },
  sendBtn: { width: 46, height: 46, borderRadius: 12, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center' },
  tip: { marginTop: 10, color: colors.muted, fontSize: 12 },
  historyRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', borderTopWidth: 1, borderTopColor: colors.border, paddingTop: 10, marginTop: 10 },
  historyTextWrap: { flex: 1, marginRight: 10 },
  historyNote: { color: colors.text, fontSize: 14, fontWeight: '600' },
  historyMeta: { color: colors.muted, fontSize: 12, marginTop: 4 },
  historyRight: { alignItems: 'flex-end' },
  historyCalories: { color: colors.text, fontWeight: '700', fontSize: 14 },
  historyRange: { color: colors.muted, fontSize: 11, marginTop: 2 },
});
