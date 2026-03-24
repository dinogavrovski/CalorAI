import React, { useState } from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors } from '@/constants/uiTheme';

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
        <Text style={styles.title}>Progress</Text>
        <Text style={styles.subtitle}>Momentum over perfection</Text>

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
                <View style={[styles.bar, { height: h, backgroundColor: i % 2 ? colors.primary : '#86EFAC' }]} />
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
  root: { flex: 1, backgroundColor: colors.bg },
  content: { padding: 20, paddingBottom: 100 },
  title: { fontSize: 30, fontWeight: '900', color: colors.text, letterSpacing: -0.5 },
  subtitle: { fontSize: 13, color: colors.muted, marginBottom: 14 },
  streakCard: { backgroundColor: '#FFF4E8', borderColor: '#F7CAA9', borderWidth: 1, borderRadius: 18, padding: 16, marginBottom: 14, shadowColor: '#B88864', shadowOpacity: 0.08, shadowRadius: 12, shadowOffset: { width: 0, height: 6 }, elevation: 2 },
  streakLabel: { color: '#9A3412', fontWeight: '700', fontSize: 12 },
  streakNumber: { color: '#7C2D12', fontWeight: '900', fontSize: 28, marginTop: 4 },
  streakHint: { color: '#9A3412', fontSize: 12, marginTop: 2 },
  tabsRow: { flexDirection: 'row', gap: 8, marginBottom: 14 },
  tabBtn: { flex: 1, height: 40, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  tabBtnActive: { backgroundColor: colors.primary },
  tabBtnIdle: { backgroundColor: '#EFE6D8' },
  tabText: { fontSize: 13, fontWeight: '700' },
  tabTextActive: { color: '#fff' },
  tabTextIdle: { color: '#54483A' },
  card: { backgroundColor: '#fff', borderColor: colors.border, borderWidth: 1, borderRadius: 18, padding: 16 },
  cardTitle: { fontSize: 16, fontWeight: '700', color: colors.text, marginBottom: 12 },
  barRow: { height: 116, flexDirection: 'row', alignItems: 'flex-end', justifyContent: 'space-between', marginBottom: 10 },
  barCol: { width: 22, alignItems: 'center', justifyContent: 'flex-end' },
  bar: { width: 20, borderRadius: 8 },
  barLabel: { marginTop: 6, color: colors.muted, fontSize: 10, fontWeight: '700' },
  muted: { color: colors.muted, fontSize: 12 },
});
