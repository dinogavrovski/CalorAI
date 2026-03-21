import React, { useState } from 'react';
import {
  View,
  ScrollView,
  StyleSheet,
  Alert,
  ActivityIndicator,
} from 'react-native';
import {
  TextInput,
  Button,
  Card,
  Chip,
  IconButton,
  Divider,
  Text,
  Surface,
  ProgressBar,
} from 'react-native-paper';
import apiService from '../services/api';
import { TextLogResponse, TextLogItem } from '../types';

export const LogFoodScreen = () => {
  const [note, setNote] = useState('');
  const [result, setResult] = useState<TextLogResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [adjustments, setAdjustments] = useState<{ [key: number]: number }>({});

  const handleLogFood = async () => {
    if (!note.trim()) {
      Alert.alert('Empty note', 'Please enter a food description');
      return;
    }

    setLoading(true);
    try {
      const response = await apiService.logFoodText(note);
      setResult(response);
      setAdjustments({}); // Reset adjustments
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.detail || 'Failed to log food');
    } finally {
      setLoading(false);
    }
  };

  const handleAdjustment = (itemIndex: number, multiplier: number) => {
    setAdjustments((prev) => ({
      ...prev,
      [itemIndex]: (prev[itemIndex] || 1) * multiplier,
    }));
  };

  const getAdjustedCalories = (item: TextLogItem, itemIndex: number) => {
    const multiplier = adjustments[itemIndex] || 1;
    return Math.round(item.calories * multiplier);
  };

  const getTotalAdjustedCalories = () => {
    if (!result) return 0;
    return result.items.reduce(
      (sum, item, idx) => sum + getAdjustedCalories(item, idx),
      0
    );
  };

  return (
    <ScrollView style={styles.container}>
      {/* Input Section */}
      <Surface style={styles.inputSection}>
        <Text style={styles.sectionTitle}>Log Your Meal</Text>
        <TextInput
          placeholder="e.g. 250g beef with 1 cup rice"
          value={note}
          onChangeText={setNote}
          multiline
          numberOfLines={4}
          mode="outlined"
          style={styles.input}
          editable={!loading}
        />
        <Button
          mode="contained"
          onPress={handleLogFood}
          loading={loading}
          disabled={loading || !note.trim()}
          style={styles.submitButton}
        >
          Analyze Meal
        </Button>
      </Surface>

      {/* Results Section */}
      {result && (
        <Surface style={styles.resultsSection}>
          <Text style={styles.sectionTitle}>Meal Breakdown</Text>

          {/* Items List */}
          {result.items.map((item, index) => (
            <Card key={index} style={styles.itemCard}>
              <Card.Content>
                {/* Food Name & Match Quality */}
                <View style={styles.itemHeader}>
                  <Text style={styles.foodName}>{item.parsed_food.toUpperCase()}</Text>
                  <Chip style={styles.calorieChip}>
                    {`${Math.round(item.calories)} cal`}
                  </Chip>
                </View>

                {/* USDA Match Info */}
                <Text style={styles.matchDescription} numberOfLines={1}>
                  {item.matched_description}
                </Text>

                {/* Quantity Display */}
                <View style={styles.quantitySection}>
                  <Text style={styles.metaText}>
                    {item.quantity} {item.unit} ({Math.round(item.estimated_grams)}g)
                  </Text>
                </View>

                {/* Nutrition Facts */}
                <Divider style={styles.divider} />
                <View style={styles.nutritionRow}>
                  <Text style={styles.metaText}>
                    {item.kcal_per_gram.toFixed(2)} kcal/g
                  </Text>
                  <Text style={styles.metaText} numberOfLines={1}>
                    Source: {item.nutrition_source}
                  </Text>
                </View>

                {/* Adjustment Controls */}
                <View style={styles.adjustmentSection}>
                  <Text style={styles.adjustmentLabel}>Adjust Portion:</Text>
                  <View style={styles.buttonGroup}>
                    <Button
                      mode="outlined"
                      compact
                      onPress={() => handleAdjustment(index, 0.5)}
                      style={styles.adjButton}
                    >
                      ½
                    </Button>
                    <Button
                      mode="outlined"
                      compact
                      onPress={() => handleAdjustment(index, 1.5)}
                      style={styles.adjButton}
                    >
                      1.5x
                    </Button>
                    <Button
                      mode="outlined"
                      compact
                      onPress={() => handleAdjustment(index, 2)}
                      style={styles.adjButton}
                    >
                      2x
                    </Button>
                  </View>
                  {adjustments[index] && (
                    <Text style={styles.adjustmentInfo}>
                      Adjusted: {getAdjustedCalories(item, index)} cal ({adjustments[index]}x)
                    </Text>
                  )}
                </View>
              </Card.Content>
            </Card>
          ))}

          {/* Total Summary */}
          <Surface style={styles.totalSummary}>
            <Text style={styles.totalLabel}>Meal Total</Text>
            <View style={styles.calorieRow}>
              <Text style={styles.totalCalories}>{getTotalAdjustedCalories()} cal</Text>
              {result.total_calorie_range && (
                <Text style={styles.rangeText}>
                  ({Math.round(result.total_calorie_range[0])}-{Math.round(result.total_calorie_range[1])} range)
                </Text>
              )}
            </View>

            {/* Progress to daily goal (2000 cal assumption) */}
            <View style={styles.progressSection}>
              <ProgressBar
                progress={getTotalAdjustedCalories() / 2000}
                style={styles.progressBar}
              />
              <Text style={styles.progressText}>
                {getTotalAdjustedCalories()} / 2000 kcal
              </Text>
            </View>

            <Button
              mode="contained"
              onPress={() => {
                Alert.alert('Success', 'Meal logged successfully!');
                setNote('');
                setResult(null);
              }}
              style={styles.logButton}
            >
              Save to History
            </Button>
          </Surface>
        </Surface>
      )}

      {/* Loading State */}
      {loading && (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" />
          <Text style={styles.loadingText}>Analyzing your meal...</Text>
        </View>
      )}
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
  inputSection: {
    padding: 16,
    borderRadius: 12,
    marginBottom: 16,
    elevation: 2,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 12,
  },
  input: {
    marginBottom: 12,
  },
  submitButton: {
    paddingVertical: 8,
  },
  resultsSection: {
    padding: 12,
    borderRadius: 12,
    marginBottom: 20,
    elevation: 2,
  },
  itemCard: {
    marginBottom: 12,
    borderRadius: 10,
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  foodName: {
    fontSize: 16,
    fontWeight: '700',
    flex: 1,
  },
  calorieChip: {
    marginLeft: 8,
  },
  matchDescription: {
    fontSize: 12,
    color: '#666',
    marginBottom: 8,
    fontStyle: 'italic',
  },
  quantitySection: {
    backgroundColor: '#f0f0f0',
    padding: 8,
    borderRadius: 6,
    marginBottom: 8,
  },
  metaText: {
    fontSize: 12,
    color: '#555',
  },
  divider: {
    marginVertical: 8,
  },
  nutritionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  adjustmentSection: {
    marginTop: 12,
  },
  adjustmentLabel: {
    fontSize: 13,
    fontWeight: '600',
    marginBottom: 8,
  },
  buttonGroup: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 8,
  },
  adjButton: {
    flex: 1,
    marginHorizontal: 4,
  },
  adjustmentInfo: {
    fontSize: 11,
    color: '#0066cc',
    textAlign: 'center',
    fontWeight: '500',
  },
  totalSummary: {
    padding: 16,
    borderRadius: 12,
    backgroundColor: '#fff',
    marginBottom: 20,
    elevation: 3,
  },
  totalLabel: {
    fontSize: 14,
    color: '#999',
    marginBottom: 8,
  },
  calorieRow: {
    flexDirection: 'row',
    alignItems: 'baseline',
    marginBottom: 12,
  },
  totalCalories: {
    fontSize: 32,
    fontWeight: '700',
    color: '#0066cc',
    marginRight: 8,
  },
  rangeText: {
    fontSize: 12,
    color: '#999',
  },
  progressSection: {
    marginBottom: 12,
  },
  progressBar: {
    height: 8,
    borderRadius: 4,
    marginBottom: 4,
  },
  progressText: {
    fontSize: 12,
    color: '#666',
    textAlign: 'right',
  },
  logButton: {
    paddingVertical: 8,
  },
  loadingContainer: {
    padding: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  loadingText: {
    marginTop: 12,
    color: '#666',
  },
});
