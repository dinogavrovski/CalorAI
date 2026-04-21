import React, { useState } from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { paperTheme } from '@/constants/paperTheme';

type TabType = 'weight' | 'sleep' | 'water';

export default function ProgressScreen() {
  const [activeTab, setActiveTab] = useState<TabType>('weight');
  const streak = 12;
  const weekHeights = [65, 72, 60, 80, 58, 88, 70];
  const labels = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

  const renderTab = (tab: TabType, label: string) => (
    <Pressable
      onPress={() => setActiveTab(tab)}
      style={[styles.tabBtn, activeTab === tab ? styles.tabBtnActive : styles.tabBtnIdle]}
    >
      <Text style={[styles.tabText, activeTab === tab ? styles.tabTextActive : styles.tabTextIdle]}>{label}</Text>
    </Pressable>
  );

  return (
    <SafeAreaView style={styles.root} edges={['top', 'left', 'right']}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.streakCard}>
          <Text style={styles.streakLabel}>Current Streak</Text>
          <Text style={styles.streakNumber}>{streak} days</Text>
          <Text style={styles.streakHint}>You are in rhythm. Keep going.</Text>
        </View>

        <View style={styles.tabsRow}>
          {renderTab('weight', 'Weight')}
          {renderTab('sleep', 'Sleep')}
          {renderTab('water', 'Water')}
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Weekly Overview</Text>
          <View style={styles.barRow}>
            {weekHeights.map((h, i) => (
              <View key={i} style={styles.barCol}>
                <View style={[styles.bar, { height: h, backgroundColor: i % 2 ? paperTheme.colors.primary : '#86EFAC' }]} />
                <Text style={styles.barLabel}>{labels[i]}</Text>
              </View>
            ))}
          </View>
          <Text style={styles.muted}>Consistent tracking beats perfect tracking.</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: paperTheme.colors.background },
  content: { padding: 20, paddingBottom: 100 },
  streakCard: { backgroundColor: paperTheme.colors.elevation.level1, borderColor: paperTheme.colors.outline, borderWidth: 1, borderRadius: 18, padding: 16, marginBottom: 14 },
  streakLabel: { color: paperTheme.colors.onSurfaceVariant, fontWeight: '700', fontSize: 12 },
  streakNumber: { color: paperTheme.colors.onSurface, fontWeight: '800', fontSize: 28, marginTop: 4 },
  streakHint: { color: paperTheme.colors.onSurfaceVariant, fontSize: 12, marginTop: 2 },
  tabsRow: { flexDirection: 'row', gap: 8, marginBottom: 14 },
  tabBtn: { flex: 1, height: 40, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  tabBtnActive: { backgroundColor: paperTheme.colors.primary },
  tabBtnIdle: { backgroundColor: paperTheme.colors.surfaceVariant },
  tabText: { fontSize: 13, fontWeight: '700' },
  tabTextActive: { color: paperTheme.colors.onPrimary },
  tabTextIdle: { color: paperTheme.colors.onSurfaceVariant },
  card: { backgroundColor: paperTheme.colors.elevation.level1, borderColor: paperTheme.colors.outline, borderWidth: 1, borderRadius: 18, padding: 16 },
  cardTitle: { fontSize: 20, lineHeight: 24, fontWeight: '600', color: paperTheme.colors.onSurface, marginBottom: 12 },
  barRow: { height: 116, flexDirection: 'row', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 10 },
  barCol: { width: 22, alignItems: 'center', justifyContent: 'flex-end' },
  bar: { width: 20, borderRadius: 8 },
  barLabel: { marginTop: 6, color: paperTheme.colors.onSurfaceVariant, fontSize: 10, fontWeight: '700' },
  muted: { color: paperTheme.colors.onSurfaceVariant, fontSize: 12 },
});
