import React, { useEffect, useRef, useState } from 'react';
import {
  Modal,
  View,
  Text,
  TextInput,
  Pressable,
  StyleSheet,
  Animated,
  Keyboard,
  Easing,
  Platform,
  ScrollView,
  Alert,
} from 'react-native';
import { X, Send } from 'lucide-react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import apiService from '@/services/api';
import { paperTheme } from '@/constants/paperTheme';
import { MealHistory, TextLogItem, TextLogResponse } from '@/types';

interface BottomSheetMealEditorProps {
  isVisible: boolean;
  meal: MealHistory | null;
  onClose: () => void;
  onSaved: () => void;
}

function toTextLogResponse(meal: MealHistory): TextLogResponse {
  return {
    note: meal.note,
    items: meal.items,
    total_calories: meal.total_calories,
    total_calorie_range: meal.total_calorie_range,
  };
}

export default function BottomSheetMealEditor({
  isVisible,
  meal,
  onClose,
  onSaved,
}: BottomSheetMealEditorProps) {
  const insets = useSafeAreaInsets();
  const [mealInput, setMealInput] = useState('');
  const [analyzeResult, setAnalyzeResult] = useState<TextLogResponse | null>(null);
  const [adjustments, setAdjustments] = useState<Record<number, number>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [isMounted, setIsMounted] = useState(isVisible);
  const fadeAnim = useRef(new Animated.Value(0)).current;
  const slideAnim = useRef(new Animated.Value(28)).current;
  const keyboardOffset = useRef(new Animated.Value(0)).current;
  const inputRef = useRef<TextInput>(null);

  useEffect(() => {
    if (isVisible) {
      setIsMounted(true);
      setMealInput(meal?.note ?? '');
      setAnalyzeResult(meal ? toTextLogResponse(meal) : null);
      setAdjustments({});
      fadeAnim.setValue(0);
      slideAnim.setValue(28);

      Animated.parallel([
        Animated.timing(fadeAnim, {
          toValue: 1,
          duration: 260,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: false,
        }),
        Animated.timing(slideAnim, {
          toValue: 0,
          duration: 320,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: false,
        }),
      ]).start(() => {
        setTimeout(() => inputRef.current?.focus(), 80);
      });
    } else {
      if (!isMounted) {
        return;
      }

      Animated.parallel([
        Animated.timing(fadeAnim, {
          toValue: 0,
          duration: 180,
          easing: Easing.in(Easing.cubic),
          useNativeDriver: false,
        }),
        Animated.timing(slideAnim, {
          toValue: 18,
          duration: 200,
          easing: Easing.in(Easing.cubic),
          useNativeDriver: false,
        }),
      ]).start(() => {
        setIsMounted(false);
      });
    }
  }, [isVisible, isMounted, fadeAnim, slideAnim, meal]);

  useEffect(() => {
    const showEvent = Platform.OS === 'ios' ? 'keyboardWillShow' : 'keyboardDidShow';
    const hideEvent = Platform.OS === 'ios' ? 'keyboardWillHide' : 'keyboardDidHide';

    const showSub = Keyboard.addListener(showEvent, (event) => {
      const duration = event.duration ?? 250;
      Animated.timing(keyboardOffset, {
        toValue: event.endCoordinates.height,
        duration,
        easing: Easing.out(Easing.cubic),
        useNativeDriver: false,
      }).start();
    });

    const hideSub = Keyboard.addListener(hideEvent, (event) => {
      const duration = event?.duration ?? 220;
      Animated.timing(keyboardOffset, {
        toValue: 0,
        duration,
        easing: Easing.out(Easing.cubic),
        useNativeDriver: false,
      }).start();
    });

    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, [keyboardOffset]);

  const getAdjustedMultiplier = (index: number) => adjustments[index] ?? 1;

  const getAdjustedCalories = (item: TextLogItem, index: number) => {
    return Math.round(item.calories * getAdjustedMultiplier(index));
  };

  const getAdjustedTotalCalories = () => {
    if (!analyzeResult) {
      return 0;
    }
    return analyzeResult.items.reduce((sum, item, index) => sum + getAdjustedCalories(item, index), 0);
  };

  const formatAdjustedQuantity = (item: TextLogItem, index: number) => {
    const multiplier = getAdjustedMultiplier(index);
    const adjusted = item.quantity * multiplier;
    return Number.isInteger(adjusted) ? adjusted.toString() : adjusted.toFixed(1);
  };

  const buildAdjustedNote = (result: TextLogResponse) => {
    return result.items
      .map((item, index) => {
        const quantity = formatAdjustedQuantity(item, index);
        const unit = item.unit ? ` ${item.unit}` : '';
        return `${quantity}${unit} ${item.parsed_food}`.trim();
      })
      .join(', ');
  };

  const handleAnalyze = async () => {
    if (!mealInput.trim()) {
      Alert.alert('Error', 'Please enter a meal description');
      return;
    }

    setIsLoading(true);
    try {
      const estimate = await apiService.logFoodText(mealInput.trim());
      setAnalyzeResult(estimate);
      setAdjustments({});
    } catch {
      Alert.alert('Error', 'Could not analyze meal');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    if (!meal || !analyzeResult) {
      return;
    }

    setIsLoading(true);
    try {
      const hasPortionEdits = Object.values(adjustments).some((multiplier) => multiplier !== 1);
      const noteToSave = hasPortionEdits ? buildAdjustedNote(analyzeResult) : mealInput.trim();

      if (!noteToSave) {
        Alert.alert('Error', 'Please enter a meal description');
        setIsLoading(false);
        return;
      }

      await apiService.updateMealHistory(meal.id, noteToSave);
      Alert.alert('Updated', 'Meal was updated');
      onClose();
      onSaved();
    } catch {
      Alert.alert('Error', 'Could not update meal');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAdjustment = (itemIndex: number, multiplier: number) => {
    setAdjustments((prev) => ({
      ...prev,
      [itemIndex]: multiplier,
    }));
  };

  const handleNoteChange = (text: string) => {
    setMealInput(text);

    if (analyzeResult) {
      // Any text edit invalidates the current estimate; require fresh analyze.
      setAnalyzeResult(null);
      setAdjustments({});
    }
  };

  return (
    <Modal visible={isMounted} animationType="none" transparent>
      <Animated.View
        style={[
          styles.container,
          {
            opacity: fadeAnim,
            transform: [{ translateY: slideAnim }],
          },
        ]}
      >
        <SafeAreaView style={styles.safeArea} edges={['top', 'left', 'right']}>
          <View style={styles.inner}>
            <View style={styles.header}>
              <Text style={styles.title}>Edit Meal</Text>
              <Pressable onPress={onClose} hitSlop={8}>
                <X size={24} color={paperTheme.colors.onSurface} />
              </Pressable>
            </View>

            <ScrollView
              style={styles.content}
              contentContainerStyle={styles.contentBody}
              keyboardShouldPersistTaps="handled"
            >
              <Text style={styles.subtitle}>Update your note and review before saving</Text>

              <TextInput
                ref={inputRef}
                style={styles.input}
                placeholder="250g chicken and 1 cup rice"
                placeholderTextColor={paperTheme.colors.onSurfaceVariant}
                value={mealInput}
                onChangeText={handleNoteChange}
                multiline
                editable={!isLoading}
                maxLength={500}
              />

              {analyzeResult ? (
                <>
                  <View style={styles.summaryCard}>
                    <Text style={styles.summaryValue}>{getAdjustedTotalCalories()} kcal</Text>
                    <Text style={styles.summaryLabel}>
                      Range {Math.round(analyzeResult.total_calorie_range[0])}-{Math.round(analyzeResult.total_calorie_range[1])} kcal
                    </Text>
                  </View>

                  {analyzeResult.items.map((item, index) => (
                    <View key={`${item.parsed_food}-${index}`} style={styles.itemCard}>
                      <Text style={styles.itemTitle}>{item.parsed_food}</Text>
                      <Text style={styles.itemMeta}>
                        Interpreted as {item.quantity} {item.unit ?? 'serving'} (~{Math.round(item.estimated_grams)}g).
                      </Text>
                      <Text style={styles.itemMeta}>
                        Nutrition source: {item.nutrition_source}
                        {item.matched_description ? ` (${item.matched_description})` : ''}
                      </Text>
                      <Text style={styles.itemCalories}>{getAdjustedCalories(item, index)} kcal</Text>

                      <View style={styles.adjustRow}>
                        <Text style={styles.adjustLabel}>Portion</Text>
                        <View style={styles.adjustButtons}>
                          {[0.5, 1, 1.5, 2].map((multiplier) => {
                            const isActive = getAdjustedMultiplier(index) === multiplier;
                            return (
                              <Pressable
                                key={multiplier}
                                style={[styles.adjustButton, isActive && styles.adjustButtonActive]}
                                onPress={() => handleAdjustment(index, multiplier)}
                                disabled={isLoading}
                              >
                                <Text style={[styles.adjustButtonText, isActive && styles.adjustButtonTextActive]}>
                                  {multiplier}x
                                </Text>
                              </Pressable>
                            );
                          })}
                        </View>
                      </View>
                    </View>
                  ))}
                </>
              ) : (
                <Text style={styles.tipText}>Tap Analyze after edits to confirm updated calories before saving.</Text>
              )}
            </ScrollView>

            <Animated.View
              style={[
                styles.actions,
                {
                  transform: [{ translateY: Animated.multiply(keyboardOffset, -1) }],
                  paddingBottom: insets.bottom + 12,
                },
              ]}
            >
              <Pressable
                style={styles.btnCancel}
                onPress={handleAnalyze}
                disabled={isLoading || !mealInput.trim()}
              >
                <Text style={styles.btnCancelText}>Analyze</Text>
              </Pressable>
              <Pressable
                style={[styles.btnLog, (isLoading || !analyzeResult) && styles.btnLogDisabled]}
                onPress={handleSave}
                disabled={isLoading || !analyzeResult}
              >
                <Send size={16} color={paperTheme.colors.onPrimary} />
                <Text style={styles.btnLogText}>{isLoading ? 'Saving...' : 'Save changes'}</Text>
              </Pressable>
            </Animated.View>
          </View>
        </SafeAreaView>
      </Animated.View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: paperTheme.colors.background,
  },
  safeArea: {
    flex: 1,
  },
  inner: {
    flex: 1,
    justifyContent: 'flex-start',
    backgroundColor: paperTheme.colors.background,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 10,
    paddingBottom: 14,
  },
  title: {
    fontSize: 24,
    lineHeight: 34,
    fontWeight: '600',
    color: paperTheme.colors.onSurface,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
    paddingTop: 4,
  },
  contentBody: {
    paddingBottom: 120,
  },
  subtitle: {
    color: paperTheme.colors.onSurfaceVariant,
    fontSize: 13,
    fontWeight: '500',
    marginBottom: 12,
  },
  input: {
    backgroundColor: paperTheme.colors.surface,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: paperTheme.colors.outline,
    color: paperTheme.colors.onSurface,
    fontSize: 16,
    paddingVertical: 14,
    paddingHorizontal: 16,
    minHeight: 96,
    maxHeight: 220,
    textAlignVertical: 'top',
    marginBottom: 12,
  },
  tipText: {
    color: paperTheme.colors.onSurfaceVariant,
    fontSize: 12,
    fontWeight: '500',
  },
  summaryCard: {
    borderWidth: 1,
    borderColor: paperTheme.colors.outline,
    backgroundColor: paperTheme.colors.surface,
    borderRadius: 14,
    padding: 14,
    marginBottom: 12,
  },
  summaryValue: {
    color: paperTheme.colors.onSurface,
    fontSize: 24,
    fontWeight: '700',
  },
  summaryLabel: {
    color: paperTheme.colors.onSurfaceVariant,
    fontSize: 12,
    marginTop: 4,
  },
  itemCard: {
    borderWidth: 1,
    borderColor: paperTheme.colors.outline,
    borderRadius: 14,
    backgroundColor: paperTheme.colors.surface,
    padding: 12,
    marginBottom: 10,
  },
  itemTitle: {
    color: paperTheme.colors.onSurface,
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 4,
  },
  itemMeta: {
    color: paperTheme.colors.onSurfaceVariant,
    fontSize: 12,
    lineHeight: 17,
  },
  itemCalories: {
    color: paperTheme.colors.onSurface,
    fontSize: 15,
    fontWeight: '700',
    marginTop: 8,
    marginBottom: 8,
  },
  adjustRow: {
    gap: 6,
  },
  adjustLabel: {
    color: paperTheme.colors.onSurface,
    fontSize: 12,
    fontWeight: '600',
  },
  adjustButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  adjustButton: {
    borderRadius: 999,
    borderWidth: 1,
    borderColor: paperTheme.colors.outline,
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: paperTheme.colors.background,
  },
  adjustButtonActive: {
    borderColor: paperTheme.colors.primary,
    backgroundColor: paperTheme.colors.primary,
  },
  adjustButtonText: {
    color: paperTheme.colors.onSurface,
    fontSize: 12,
    fontWeight: '700',
  },
  adjustButtonTextActive: {
    color: paperTheme.colors.onPrimary,
  },
  actions: {
    position: 'absolute',
    left: 20,
    right: 20,
    bottom: 0,
    flexDirection: 'row',
    gap: 10,
  },
  btnCancel: {
    flex: 1,
    height: 50,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: paperTheme.colors.outline,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: paperTheme.colors.surfaceVariant,
  },
  btnCancelText: {
    color: paperTheme.colors.onSurface,
    fontWeight: '700',
    fontSize: 16,
  },
  btnLog: {
    flex: 1,
    height: 50,
    borderRadius: 14,
    backgroundColor: paperTheme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 8,
  },
  btnLogDisabled: {
    opacity: 0.6,
  },
  btnLogText: {
    color: paperTheme.colors.onPrimary,
    fontWeight: '700',
    fontSize: 16,
  },
});
