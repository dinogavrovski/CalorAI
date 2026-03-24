import React from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '@/context/AuthContext';
import { colors } from '@/constants/uiTheme';

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
  root: { flex: 1, backgroundColor: colors.bg },
  content: { padding: 20, paddingBottom: 100 },
  title: { fontSize: 28, fontWeight: '800', color: colors.text, marginBottom: 14 },
  card: { backgroundColor: '#fff', borderColor: colors.border, borderWidth: 1, borderRadius: 16, padding: 18, alignItems: 'center', marginBottom: 14 },
  avatar: { width: 72, height: 72, borderRadius: 36, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center', marginBottom: 10 },
  avatarText: { color: '#fff', fontSize: 28, fontWeight: '800' },
  name: { fontSize: 20, fontWeight: '800', color: colors.text },
  email: { fontSize: 13, color: colors.muted, marginTop: 4 },
  section: { backgroundColor: '#fff', borderColor: colors.border, borderWidth: 1, borderRadius: 16, padding: 16, gap: 10 },
  sectionTitle: { fontSize: 16, fontWeight: '700', color: colors.text, marginBottom: 4 },
  item: { flexDirection: 'row', justifyContent: 'space-between' },
  itemLabel: { color: colors.muted, fontSize: 13 },
  itemValue: { color: colors.text, fontWeight: '700', fontSize: 13 },
  logoutBtn: { marginTop: 14, height: 46, borderRadius: 12, backgroundColor: '#FEE2E2', alignItems: 'center', justifyContent: 'center' },
  logoutText: { color: '#B91C1C', fontWeight: '700' },
});
