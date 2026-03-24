import React, { useState } from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors } from '@/constants/uiTheme';

type TabType = 'weight' | 'sleep' | 'water';

export default function ProgressScreen() {
  const [activeTab, setActiveTab] = useState<TabType>('weight');
  const streak = 12;

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
        <Text style={styles.title}>Progress</Text>
        <Text style={styles.subtitle}>Track your journey</Text>

        <View style={styles.streakCard}>
          <Text style={styles.streakLabel}>Current Streak</Text>
          <Text style={styles.streakNumber}>{streak} days</Text>
          <Text style={styles.streakHint}>Keep it going</Text>
        </View>

        <View style={styles.tabsRow}>
          {renderTab('weight', 'Weight')}
          {renderTab('sleep', 'Sleep')}
          {renderTab('water', 'Water')}
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Weekly Overview</Text>
          <View style={styles.barRow}>
            {[65, 72, 60, 80, 58, 88, 70].map((h, i) => (
              <View key={i} style={styles.barCol}>
                <View style={[styles.bar, { height: h, backgroundColor: i % 2 ? colors.primary : '#86EFAC' }]} />
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
  root: { flex: 1, backgroundColor: colors.bg },
  content: { padding: 20, paddingBottom: 100 },
  title: { fontSize: 28, fontWeight: '800', color: colors.text },
  subtitle: { fontSize: 13, color: colors.muted, marginBottom: 14 },
  streakCard: { backgroundColor: '#FFF7ED', borderColor: '#FDBA74', borderWidth: 1, borderRadius: 16, padding: 16, marginBottom: 14 },
  streakLabel: { color: '#9A3412', fontWeight: '700', fontSize: 12 },
  streakNumber: { color: '#7C2D12', fontWeight: '900', fontSize: 28, marginTop: 4 },
  streakHint: { color: '#9A3412', fontSize: 12, marginTop: 2 },
  tabsRow: { flexDirection: 'row', gap: 8, marginBottom: 14 },
  tabBtn: { flex: 1, height: 40, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  tabBtnActive: { backgroundColor: colors.primary },
  tabBtnIdle: { backgroundColor: '#E2E8F0' },
  tabText: { fontSize: 13, fontWeight: '700' },
  tabTextActive: { color: '#fff' },
  tabTextIdle: { color: '#334155' },
  card: { backgroundColor: '#fff', borderColor: colors.border, borderWidth: 1, borderRadius: 16, padding: 16 },
  cardTitle: { fontSize: 16, fontWeight: '700', color: colors.text, marginBottom: 12 },
  barRow: { height: 96, flexDirection: 'row', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 10 },
  barCol: { width: 22, alignItems: 'center', justifyContent: 'flex-end' },
  bar: { width: 20, borderRadius: 8 },
  muted: { color: colors.muted, fontSize: 12 },
});
