import React, { useEffect, useMemo, useRef, useState } from 'react';
import { View, ScrollView, StyleSheet, Pressable, Animated, Easing } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect } from '@react-navigation/native';
import { BellDot, Flame, Pencil, User, CheckCircle2, Clock } from 'lucide-react-native';
import { useRouter } from 'expo-router';
import { Avatar, Card, Chip, Surface, Text } from 'react-native-paper';
import Svg, { Circle, Path } from 'react-native-svg';
import { useAuth } from '@/context/AuthContext';
import apiService from '@/services/api';
import { paperTheme } from '@/constants/paperTheme';

const goalCalories = 2200;

// Circular Progress Component
const CircularProgress = ({ 
  percentage, 
  size = 200, 
  strokeWidth = 12, 
  value, 
  label 
}: { 
  percentage: number; 
  size?: number; 
  strokeWidth?: number; 
  value: string | number; 
  label: string;
}) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * 2 * Math.PI;
  const offset = circumference - Math.min(percentage / 100, 1) * circumference;
  
  return (
    <View style={{ alignItems: 'center', justifyContent: 'center', marginBottom: 12 }}>
      <View style={styles.progressContainer}>
        <Svg width={size} height={size}>
          {/* Background circle */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="#3A3A3A"
            strokeWidth={strokeWidth}
            fill="none"
          />
          {/* Progress circle */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="#E58A3A"
            strokeWidth={strokeWidth}
            fill="none"
            strokeDasharray={circumference}
            strokeDashoffset={offset}
            strokeLinecap="round"
            transform={`rotate(-90 ${size / 2} ${size / 2})`}
          />
        </Svg>
        {/* Center text - positioned absolutely over SVG */}
        <View style={[styles.circleCenter, { width: size, height: size }]}>
          <Text style={styles.circleValue}>{value}</Text>
          <Text style={styles.circleLabel}>{label}</Text>
        </View>
      </View>
    </View>
  );
};

// Water Circle Progress Component
const WaterCircle = ({ 
  percentage, 
  size = 200, 
  strokeWidth = 12, 
  value, 
  label 
}: { 
  percentage: number; 
  size?: number; 
  strokeWidth?: number; 
  value: string | number; 
  label: string;
}) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * 2 * Math.PI;
  const offset = circumference - Math.min(percentage / 100, 1) * circumference;
  
  return (
    <View style={{ alignItems: 'center', justifyContent: 'center', marginBottom: 12 }}>
      <View style={styles.progressContainer}>
        <Svg width={size} height={size}>
          {/* Background circle */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="#3A3A3A"
            strokeWidth={strokeWidth}
            fill="none"
          />
          {/* Progress circle */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="#4FC3F7"
            strokeWidth={strokeWidth}
            fill="none"
            strokeDasharray={circumference}
            strokeDashoffset={offset}
            strokeLinecap="round"
            transform={`rotate(-90 ${size / 2} ${size / 2})`}
          />
        </Svg>
        {/* Center text - positioned absolutely over SVG */}
        <View style={[styles.circleCenter, { width: size, height: size }]}>
          <Text style={styles.circleValue}>{value}</Text>
          <Text style={styles.circleLabel}>{label}</Text>
        </View>
      </View>
    </View>
  );
};

// Macros Concentric Progress Component
const MacrosCircle = ({
  proteinPercentage,
  carbsPercentage,
  fatsPercentage,
  size = 200,
  strokeWidth = 14,
}: {
  proteinPercentage: number;
  carbsPercentage: number;
  fatsPercentage: number;
  size?: number;
  strokeWidth?: number;
}) => {
  const createCircleData = (percentage: number, ringIndex: number) => {
    const baseRadius = (size - strokeWidth) / 2;
    const radius = baseRadius - ringIndex * (strokeWidth + 8);
    const circumference = radius * 2 * Math.PI;
    const offset = circumference - Math.min(percentage / 100, 1) * circumference;
    return { radius, circumference, offset };
  };

  const proteinData = createCircleData(proteinPercentage, 0);
  const carbsData = createCircleData(carbsPercentage, 1);
  const fatsData = createCircleData(fatsPercentage, 2);

  return (
    <View style={{ alignItems: 'center', justifyContent: 'center', marginBottom: 12 }}>
      <View style={styles.macrosProgressContainer}>
        <Svg width={size} height={size}>
          {/* Protein rings - blue */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={proteinData.radius}
            stroke="#4A90E2"
            strokeWidth={strokeWidth}
            fill="none"
            opacity={0.2}
          />
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={proteinData.radius}
            stroke="#4A90E2"
            strokeWidth={strokeWidth}
            fill="none"
            strokeDasharray={proteinData.circumference}
            strokeDashoffset={proteinData.offset}
            strokeLinecap="round"
            transform={`rotate(-90 ${size / 2} ${size / 2})`}
          />

          {/* Carbs rings - green */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={carbsData.radius}
            stroke="#6FCF97"
            strokeWidth={strokeWidth}
            fill="none"
            opacity={0.2}
          />
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={carbsData.radius}
            stroke="#6FCF97"
            strokeWidth={strokeWidth}
            fill="none"
            strokeDasharray={carbsData.circumference}
            strokeDashoffset={carbsData.offset}
            strokeLinecap="round"
            transform={`rotate(-90 ${size / 2} ${size / 2})`}
          />

          {/* Fats rings - orange */}
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={fatsData.radius}
            stroke="#FFB766"
            strokeWidth={strokeWidth}
            fill="none"
            opacity={0.2}
          />
          <Circle
            cx={size / 2}
            cy={size / 2}
            r={fatsData.radius}
            stroke="#FFB766"
            strokeWidth={strokeWidth}
            fill="none"
            strokeDasharray={fatsData.circumference}
            strokeDashoffset={fatsData.offset}
            strokeLinecap="round"
            transform={`rotate(-90 ${size / 2} ${size / 2})`}
          />
        </Svg>
        {/* Center text */}
        <View style={[styles.macrosCircleCenter, { width: size, height: size }]}>
          <Text style={styles.macrosCircleLabel}>Macros</Text>
        </View>
      </View>
    </View>
  );
};

export default function HomeScreen() {
  const { user } = useAuth();
  const router = useRouter();
  const [dailyCalories, setDailyCalories] = useState(0);
  const [recentMeals, setRecentMeals] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState<'calories' | 'macros' | 'water'>('calories');
  const tabs = ['calories', 'macros', 'water'] as const;
  const tabHighlight = useRef(tabs.map((_, index) => new Animated.Value(index === 0 ? 1 : 0))).current;

  useEffect(() => {
    const activeIndex = tabs.indexOf(activeTab);
    Animated.parallel(
      tabHighlight.map((anim, index) =>
        Animated.timing(anim, {
          toValue: index === activeIndex ? 1 : 0,
          duration: 180,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: true,
        })
      )
    ).start();
  }, [activeTab, tabHighlight]);

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

  const getMealName = (meal: any) => {
    const note = (meal?.note || 'Meal').toString().trim();
    if (note) {
      return note.charAt(0).toUpperCase() + note.slice(1);
    }

    const parsedItems = (meal?.items || [])
      .map((item: any) => item?.parsed_food?.trim())
      .filter(Boolean)
      .slice(0, 3)
      .join(', ');

    if (parsedItems) {
      return parsedItems.charAt(0).toUpperCase() + parsedItems.slice(1);
    }

    return 'Meal';
  };

  const getMealDose = (meal: any) => {
    const gramsFromItems = (meal?.items || []).reduce(
      (sum: number, item: any) => sum + (item?.estimated_grams || 0),
      0
    );
    if (gramsFromItems > 0) {
      return `${Math.round(gramsFromItems)}g`;
    }

    const note = (meal?.note || '').toString();
    const match = note.match(/(\d+)\s*(g|grams?)/i);
    return match ? `${match[1]}g` : '';
  };

  const weekDays = useMemo(
    () => [
      { day: 'Fri', date: 20 },
      { day: 'Sat', date: 21 },
      { day: 'Sun', date: 22 },
      { day: 'Mon', date: 23 },
      { day: 'Tue', date: 24 },
      { day: 'Wed', date: 25, active: true },
      { day: 'Thu', date: 26 },
    ],
    []
  );
  const activeDayIndex = weekDays.findIndex((item) => item.active);

  return (
    <SafeAreaView style={styles.root} edges={['top', 'left', 'right']}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.headerRow}>
          <Text style={styles.dailySummaryLabel}>Daily Summary</Text>

          <View style={styles.headerActions}>
            <Chip icon={() => <Flame size={16} color="#FFB089" />} style={styles.fireChip} textStyle={styles.fireChipText}>
              0
            </Chip>

            <Pressable style={styles.profileBtn} onPress={() => router.push('/profile')}>
              <User size={20} color="#DCE0EF" />
            </Pressable>
          </View>
        </View>

        {/* Tab buttons - outside of card */}
        <View style={styles.heroTabs}>
          {tabs.map((tab, index) => (
            <Pressable
              key={tab}
              style={({ pressed }) => [
                styles.heroTab,
                pressed && styles.heroTabPressed,
              ]}
              onPress={() => setActiveTab(tab)}
            >
              <Animated.View
                pointerEvents="none"
                style={[
                  styles.heroTabActive,
                  {
                    opacity: tabHighlight[index],
                  },
                ]}
              />
              <Text
                style={[
                  styles.heroTabLabel,
                  activeTab === tab && styles.heroTabLabelActive,
                ]}
              >
                {tab.charAt(0).toUpperCase() + tab.slice(1)}
              </Text>
            </Pressable>
          ))}
        </View>

        <View style={styles.heroCardView}>
          {/* Content */}
          <View style={styles.heroContent}>
            {activeTab === 'calories' && (
              <View style={styles.caloriesView}>
                <CircularProgress 
                  percentage={(dailyCalories / goalCalories) * 100} 
                  size={200} 
                  strokeWidth={12}
                  value={Math.round(dailyCalories)}
                  label="Calories"
                />
              </View>
            )}

            {activeTab === 'macros' && (
              <View style={styles.macrosView}>
                <MacrosCircle 
                  proteinPercentage={(Math.max(0, 130 - Math.round((dailyCalories / goalCalories) * 130)) / 130) * 100}
                  carbsPercentage={(Math.max(0, 250 - Math.round((dailyCalories / goalCalories) * 250)) / 250) * 100}
                  fatsPercentage={(Math.max(0, 60 - Math.round((dailyCalories / goalCalories) * 60)) / 60) * 100}
                  size={200}
                  strokeWidth={12}
                />
              </View>
            )}

            {activeTab === 'water' && (
              <View style={styles.waterView}>
                <WaterCircle 
                  percentage={(6 / 8) * 100} 
                  size={200} 
                  strokeWidth={12}
                  value="6/8"
                  label="cups"
                />
              </View>
            )}
          </View>
        </View>

        {/* Calorie Stats Cards - shown when on calories tab */}
        {activeTab === 'calories' && (
          <View style={styles.calorieStatsRow}>
            <View style={styles.calorieStatCard}>
              <CheckCircle2 size={20} color="#6FCF97" style={styles.calorieStatIcon} />
              <View>
                <Text style={styles.calorieStatLabel}>Consumed</Text>
                <Text style={styles.calorieStatValue}>{Math.round(dailyCalories)} <Text style={styles.calorieStatUnit}>cal</Text></Text>
              </View>
            </View>
            <View style={styles.calorieStatCard}>
              <Clock size={20} color="#FFB766" style={styles.calorieStatIcon} />
              <View>
                <Text style={styles.calorieStatLabel}>Remaining</Text>
                <Text style={styles.calorieStatValue}>{Math.max(0, Math.round(goalCalories - dailyCalories))} <Text style={styles.calorieStatUnit}>cal</Text></Text>
              </View>
            </View>
          </View>
        )}

        {/* Macro Stats Cards - shown when on macros tab */}
        {activeTab === 'macros' && (
          <View style={styles.macroStatsRow}>
            <View style={styles.macroStatCard}>
              <Text style={styles.macroStatCardLabel}>Protein</Text>
              <Text style={styles.macroStatCardValue}>{Math.max(0, 130 - Math.round((dailyCalories / goalCalories) * 130))}</Text>
              <Text style={styles.macroStatCardUnit}>/180g</Text>
            </View>
            <View style={styles.macroStatCard}>
              <Text style={styles.macroStatCardLabel}>Carbs</Text>
              <Text style={styles.macroStatCardValue}>{Math.max(0, 250 - Math.round((dailyCalories / goalCalories) * 250))}</Text>
              <Text style={styles.macroStatCardUnit}>/292g</Text>
            </View>
            <View style={styles.macroStatCard}>
              <Text style={styles.macroStatCardLabel}>Fats</Text>
              <Text style={styles.macroStatCardValue}>{Math.max(0, 60 - Math.round((dailyCalories / goalCalories) * 60))}</Text>
              <Text style={styles.macroStatCardUnit}>/66g</Text>
            </View>
          </View>
        )}

        {/* Water encouragement text - shown when on water tab */}
        {activeTab === 'water' && (
          <View style={styles.waterStatsContainer}>
            <Text style={styles.waterSubtext}>Stay hydrated!</Text>
          </View>
        )}

        <View style={styles.sectionHeader}>
          <Text variant="headlineSmall" style={styles.sectionTitle}>Recently logged</Text>
        </View>

        <View style={styles.recentList}>
          {recentMeals.length === 0 ? (
            <View style={styles.emptyWrap}>
              <Avatar.Text size={42} label="+" style={styles.emptyAvatar} labelStyle={styles.emptyAvatarLabel} />
              <Text style={styles.emptyText}>Tap + to add your first meal of the day</Text>
            </View>
          ) : (
            recentMeals.slice(0, 3).map((meal, index) => (
              <View key={meal.id || index} style={styles.recentItem}>
                <View style={styles.recentMealLeft}>
                  <Text style={styles.recentMealName} numberOfLines={1}>{getMealName(meal)}</Text>
                  {!!getMealDose(meal) && <Text style={styles.recentMealDose}>{getMealDose(meal)}</Text>}
                </View>
                <View style={styles.recentRight}>
                  <Text style={styles.recentMealKcal}>{Math.round(meal.total_calories || 0)} kcal</Text>
                  <View style={styles.editIconBtn}>
                    <Pencil size={15} color={paperTheme.colors.onSurfaceVariant} />
                  </View>
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
  root: {
    flex: 1,
    backgroundColor: paperTheme.colors.background,
  },
  content: {
    paddingHorizontal: 18,
    paddingTop: 10,
    paddingBottom: 130,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  brand: {
    color: '#F3F4FA',
    fontWeight: '600',
    fontSize: 24,
    lineHeight: 34,
  },
  fireChip: {
    backgroundColor: '#2A2A2A',
  },
  fireChipText: {
    color: '#EFEFEF',
    fontWeight: '500',
  },
  profileBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#2A2A2A',
    borderWidth: 1,
    borderColor: '#3B3B3B',
    alignItems: 'center',
    justifyContent: 'center',
  },
  weekRow: {
    gap: 10,
    paddingVertical: 4,
    paddingLeft: 18,
    paddingRight: 18,
    marginLeft: -18,
    marginRight: -18,
    marginBottom: 8,
  },
  dayWrap: {
    alignItems: 'center',
    width: 46,
  },
  dayText: {
    color: '#8F8F8F',
    marginBottom: 6,
    fontSize: 12,
    fontWeight: '500',
  },
  dayTextActive: {
    color: '#F2F2F2',
  },
  dayTextPast: {
    color: '#6C6C6C',
  },
  dayCircle: {
    width: 46,
    height: 46,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#222222',
    borderWidth: 1,
    borderColor: '#353535',
  },
  dayCirclePast: {
    backgroundColor: '#1A1A1A',
    borderColor: '#2C2C2C',
  },
  dayCircleActive: {
    backgroundColor: '#323232',
    borderColor: '#E58A3A',
  },
  dayNumber: {
    color: '#D3D3D3',
    fontSize: 15,
    fontWeight: '500',
  },
  dayNumberActive: {
    color: '#F2F2F2',
  },
  dayNumberPast: {
    color: '#7F7F7F',
  },
  heroCardView: {
    marginTop: 12,
  },
  heroRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  heroNumber: {
    color: '#F2F2F2',
    fontWeight: '600',
  },
  heroLabel: {
    color: '#DBDBDB',
    fontWeight: '500',
    marginTop: 2,
  },
  progress: {
    marginTop: 14,
    width: 185,
    borderRadius: 99,
    height: 8,
    backgroundColor: '#3A3A3A',
  },
  progressLabel: {
    color: '#9C9C9C',
    fontSize: 11,
    marginTop: 8,
    fontWeight: '500',
  },
  heroBadge: {
    width: 86,
    height: 86,
    borderRadius: 43,
    backgroundColor: '#2E2E2E',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#3D3D3D',
  },
  macroRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 12,
  },
  macroCard: {
    flex: 1,
    borderRadius: 12,
    backgroundColor: '#242424',
    borderWidth: 1,
    borderColor: '#363636',
  },
  macroContent: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    minHeight: 86,
    paddingHorizontal: 16,
    paddingVertical: 4,
  },
  macroTextBlock: {
    flex: 1,
    justifyContent: 'center',
  },
  macroNumber: {
    color: '#F7F8FC',
    fontSize: 16,
    fontWeight: '600',
    textAlign: 'left',
  },
  macroLabel: {
    color: '#CACEE0',
    fontSize: 10,
    marginTop: 2,
    fontWeight: '500',
    textAlign: 'left',
    lineHeight: 14,
  },
  macroEmoji: {
    width: 28,
    fontSize: 22,
    lineHeight: 22,
    textAlign: 'center',
    includeFontPadding: false,
  },
  sectionHeader: {
    marginTop: 26,
    marginBottom: 10,
  },
  sectionTitle: {
    color: '#F2F2F2',
    fontWeight: '600',
    fontSize: 20,
    lineHeight: 24,
  },
  dailySummaryLabel: {
    color: '#F2F2F2',
    fontWeight: '600',
    fontSize: 20,
    marginBottom: 0,
  },
  recentList: {
    backgroundColor: 'transparent',
  },
  emptyWrap: {
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 120,
  },
  emptyAvatar: {
    backgroundColor: '#313131',
    marginBottom: 10,
  },
  emptyAvatarLabel: {
    color: '#7CD2E8',
    fontWeight: '700',
  },
  emptyText: {
    color: '#A8A8A8',
    fontSize: 14,
    fontWeight: '500',
  },
  recentItem: {
    paddingHorizontal: 14,
    paddingVertical: 12,
    backgroundColor: '#1F1F1F',
    borderWidth: 1,
    borderColor: '#323232',
    borderRadius: 10,
    marginBottom: 10,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  recentMealLeft: {
    flex: 1,
    marginRight: 12,
  },
  recentMealName: {
    color: '#ECECEC',
    fontSize: 20,
    lineHeight: 24,
    fontWeight: '600',
  },
  recentMealDose: {
    color: '#8E8E8E',
    fontSize: 12,
    marginTop: 2,
    fontWeight: '500',
  },
  recentRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  recentMealKcal: {
    color: '#E58A3A',
    fontWeight: '700',
    fontSize: 16,
    marginTop: 0,
  },
  editIconBtn: {
    width: 30,
    height: 30,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#4A4A4A',
    backgroundColor: '#171717',
    alignItems: 'center',
    justifyContent: 'center',
  },
  heroTabs: {
    flexDirection: 'row',
    marginBottom: 0,
    marginTop: 12,
    gap: 8,
    backgroundColor: '#2A2A2A',
    borderRadius: 8,
    padding: 5,
    position: 'relative',
  },
  heroTab: {
    flex: 1,
    paddingVertical: 10,
    paddingHorizontal: 16,
    alignItems: 'center',
    borderRadius: 8,
    backgroundColor: 'transparent',
    overflow: 'hidden',
  },
  heroTabActive: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: '#1F1F1F',
    borderRadius: 8,
  },
  heroTabPressed: {
    opacity: 0.92,
  },
  heroTabLabel: {
    color: '#8F8F8F',
    fontSize: 14,
    fontWeight: '500',
    zIndex: 1,
  },
  heroTabLabelActive: {
    color: '#F2F2F2',
  },
  heroContent: {
    paddingVertical: 24,
    paddingHorizontal: 16,
    alignItems: 'center',
  },
  caloriesView: {
    alignItems: 'center',
    width: '100%',
  },
  progressContainer: {
    position: 'relative',
    alignItems: 'center',
    justifyContent: 'center',
  },
  circleCenter: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
  circleValue: {
    color: '#F2F2F2',
    fontSize: 48,
    fontWeight: '700',
    lineHeight: 56,
  },
  circleLabel: {
    color: '#9C9C9C',
    fontSize: 12,
    fontWeight: '500',
    marginTop: 4,
  },
  calorieStatsRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 12,
    marginHorizontal: 18,
  },
  calorieStatCard: {
    flex: 1,
    borderRadius: 10,
    backgroundColor: '#242424',
    borderWidth: 1,
    borderColor: '#363636',
    padding: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
  },
  calorieStatIcon: {
    flexShrink: 0,
  },
  calorieStatLabel: {
    color: '#8F8F8F',
    fontSize: 14,
    fontWeight: '500',
    marginBottom: 4,
  },
  calorieStatValue: {
    color: '#F2F2F2',
    fontSize: 16,
    fontWeight: '700',
  },
  calorieStatUnit: {
    color: '#F2F2F2',
    fontSize: 14,
    fontWeight: '500',
  },
  macrosView: {
    width: '100%',
    alignItems: 'center',
  },
  macrosProgressContainer: {
    position: 'relative',
    alignItems: 'center',
    justifyContent: 'center',
  },
  macrosCircleCenter: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
  macrosCircleLabel: {
    color: '#9C9C9C',
    fontSize: 14,
    fontWeight: '500',
  },
  macroStatsRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 12,
    marginHorizontal: 18,
  },
  macroStatCard: {
    flex: 1,
    borderRadius: 10,
    backgroundColor: '#242424',
    borderWidth: 1,
    borderColor: '#363636',
    padding: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  macroStatCardLabel: {
    color: '#8F8F8F',
    fontSize: 12,
    fontWeight: '500',
    marginBottom: 6,
  },
  macroStatCardValue: {
    color: '#F2F2F2',
    fontSize: 20,
    fontWeight: '700',
    lineHeight: 24,
  },
  macroStatCardUnit: {
    color: '#6B6B6B',
    fontSize: 10,
    fontWeight: '500',
    marginTop: 2,
  },
  macroStatItem: {
    alignItems: 'flex-start',
  },
  macroStatColor: {
    color: '#8F8F8F',
    fontSize: 11,
    fontWeight: '500',
    marginBottom: 4,
  },
  macroStatText: {
    color: '#F2F2F2',
    fontSize: 14,
    fontWeight: '600',
  },
  macroItemRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
  },
  macroItemSmall: {
    alignItems: 'center',
    flex: 1,
    paddingHorizontal: 8,
  },
  macroItemLabel: {
    color: '#8F8F8F',
    fontSize: 12,
    fontWeight: '500',
    marginBottom: 6,
  },
  macroItemValue: {
    color: '#F2F2F2',
    fontSize: 20,
    fontWeight: '700',
    lineHeight: 28,
  },
  macroItemGoal: {
    color: '#6B6B6B',
    fontSize: 11,
    fontWeight: '500',
    marginTop: 4,
  },
  waterView: {
    width: '100%',
    alignItems: 'center',
  },
  waterContainer: {
    alignItems: 'center',
    marginBottom: 20,
  },
  waterValue: {
    color: '#F2F2F2',
    fontSize: 48,
    fontWeight: '700',
  },
  waterUnit: {
    color: '#8F8F8F',
    fontSize: 14,
    fontWeight: '500',
    marginTop: 4,
  },
  waterSubtext: {
    color: '#6B6B6B',
    fontSize: 12,
    fontWeight: '500',
    marginTop: 8,
  },
  waterStatsContainer: {
    marginTop: 12,
    marginHorizontal: 18,
    gap: 12,
  },
  waterProgress: {
    width: '100%',
    height: 8,
    borderRadius: 99,
    backgroundColor: '#3A3A3A',
  },
});
