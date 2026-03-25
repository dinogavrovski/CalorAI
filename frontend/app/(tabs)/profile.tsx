import React from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '@/context/AuthContext';
import { paperTheme } from '@/constants/paperTheme';

export default function ProfileScreen() {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    Alert.alert('Log Out', 'Are you sure you want to log out?', [
      { text: 'Cancel' },
      { text: 'Log Out', style: 'destructive', onPress: () => logout() },
    ]);
  };

  return (
    <SafeAreaView style={styles.root} edges={['top', 'left', 'right']}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.title}>Profile</Text>
        <Text style={styles.subtitle}>Your nutrition identity</Text>

        <View style={styles.card}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{(user?.username || 'U').slice(0, 1).toUpperCase()}</Text>
          </View>
          <Text style={styles.name}>{user?.username || 'User'}</Text>
          <Text style={styles.email}>{user?.email || 'user@email.com'}</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Goals</Text>
          <View style={styles.item}><Text style={styles.itemLabel}>Daily Calories</Text><Text style={styles.itemValue}>2200 kcal</Text></View>
          <View style={styles.item}><Text style={styles.itemLabel}>Protein</Text><Text style={styles.itemValue}>180 g</Text></View>
          <View style={styles.item}><Text style={styles.itemLabel}>Carbs</Text><Text style={styles.itemValue}>250 g</Text></View>
        </View>

        <Pressable style={styles.logoutBtn} onPress={handleLogout}>
          <Text style={styles.logoutText}>Log Out</Text>
        </Pressable>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: paperTheme.colors.background },
  content: { padding: 20, paddingBottom: 100 },
  title: { fontSize: 24, lineHeight: 34, fontWeight: '600', color: paperTheme.colors.onBackground },
  subtitle: { fontSize: 13, fontWeight: '500', color: paperTheme.colors.onSurfaceVariant, marginBottom: 14 },
  card: { backgroundColor: paperTheme.colors.elevation.level1, borderColor: paperTheme.colors.outline, borderWidth: 1, borderRadius: 18, padding: 18, alignItems: 'center', marginBottom: 14 },
  avatar: { width: 72, height: 72, borderRadius: 36, backgroundColor: paperTheme.colors.primary, alignItems: 'center', justifyContent: 'center', marginBottom: 10 },
  avatarText: { color: paperTheme.colors.onPrimary, fontSize: 28, fontWeight: '800' },
  name: { fontSize: 20, fontWeight: '800', color: paperTheme.colors.onSurface },
  email: { fontSize: 13, color: paperTheme.colors.onSurfaceVariant, marginTop: 4 },
  section: { backgroundColor: paperTheme.colors.elevation.level1, borderColor: paperTheme.colors.outline, borderWidth: 1, borderRadius: 18, padding: 16, gap: 10 },
  sectionTitle: { fontSize: 20, lineHeight: 24, fontWeight: '600', color: paperTheme.colors.onSurface, marginBottom: 4 },
  item: { flexDirection: 'row', justifyContent: 'space-between', backgroundColor: paperTheme.colors.surfaceVariant, borderWidth: 1, borderColor: paperTheme.colors.outline, borderRadius: 12, paddingVertical: 10, paddingHorizontal: 12 },
  itemLabel: { color: paperTheme.colors.onSurfaceVariant, fontSize: 13 },
  itemValue: { color: paperTheme.colors.onSurface, fontWeight: '700', fontSize: 13 },
  logoutBtn: { marginTop: 14, height: 48, borderRadius: 14, backgroundColor: '#3A1F2A', borderWidth: 1, borderColor: '#5A3140', alignItems: 'center', justifyContent: 'center' },
  logoutText: { color: '#FF9FB2', fontWeight: '700' },
});
