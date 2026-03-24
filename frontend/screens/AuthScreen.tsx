import React, { useState } from 'react';
import { View, Text, TextInput, Pressable, ScrollView, Alert, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { colors } from '@/constants/uiTheme';

type AuthMode = 'login' | 'signup';

export default function AuthScreen() {
  const { login, register, isLoading } = useAuth();
  const router = useRouter();
  const [mode, setMode] = useState<AuthMode>('login');
  const [errorMessage, setErrorMessage] = useState('');

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const handleLogin = async () => {
    setErrorMessage('');
    if (!formData.username.trim() || !formData.password.trim()) {
      const message = 'Please fill in all fields';
      Alert.alert('Error', message);
      setErrorMessage(message);
      return;
    }

    try {
      await login(formData.username, formData.password);
      router.replace('/(tabs)');
    } catch (error: any) {
      const message = error?.response?.data?.detail || 'Invalid credentials. Please try again.';
      Alert.alert('Login Failed', message);
      setErrorMessage(message);
    }
  };

  const handleSignup = async () => {
    setErrorMessage('');
    if (!formData.username.trim() || !formData.email.trim() || !formData.password.trim()) {
      const message = 'Please fill in all fields';
      Alert.alert('Error', message);
      setErrorMessage(message);
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      const message = 'Passwords do not match';
      Alert.alert('Error', message);
      setErrorMessage(message);
      return;
    }

    if (formData.password.length < 6) {
      const message = 'Password must be at least 6 characters';
      Alert.alert('Error', message);
      setErrorMessage(message);
      return;
    }

    try {
      await register(formData.username, formData.email, formData.password);
      router.replace('/(tabs)');
    } catch (error: any) {
      const message = error?.response?.data?.detail || 'Unable to create account. Please try again.';
      Alert.alert('Signup Failed', message);
      setErrorMessage(message);
    }
  };

  return (
    <SafeAreaView style={styles.root}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <Text style={styles.brand}>CalAI</Text>
          <Text style={styles.tagline}>Smart Nutrition Tracking</Text>
        </View>

        <View style={styles.card}>
          <View style={styles.modeRow}>
            <Pressable style={[styles.modeBtn, mode === 'login' && styles.modeBtnActive]} onPress={() => setMode('login')}>
              <Text style={[styles.modeTxt, mode === 'login' && styles.modeTxtActive]}>Login</Text>
            </Pressable>
            <Pressable style={[styles.modeBtn, mode === 'signup' && styles.modeBtnActive]} onPress={() => setMode('signup')}>
              <Text style={[styles.modeTxt, mode === 'signup' && styles.modeTxtActive]}>Sign Up</Text>
            </Pressable>
          </View>

          <TextInput
            style={styles.input}
            placeholder="Username"
            placeholderTextColor="#94A3B8"
            value={formData.username}
            onChangeText={(text) => setFormData({ ...formData, username: text })}
            editable={!isLoading}
          />

          {mode === 'signup' && (
            <TextInput
              style={styles.input}
              placeholder="Email"
              placeholderTextColor="#94A3B8"
              value={formData.email}
              keyboardType="email-address"
              onChangeText={(text) => setFormData({ ...formData, email: text })}
              editable={!isLoading}
            />
          )}

          <TextInput
            style={styles.input}
            placeholder="Password"
            placeholderTextColor="#94A3B8"
            secureTextEntry
            value={formData.password}
            onChangeText={(text) => setFormData({ ...formData, password: text })}
            editable={!isLoading}
          />

          {mode === 'signup' && (
            <TextInput
              style={styles.input}
              placeholder="Confirm Password"
              placeholderTextColor="#94A3B8"
              secureTextEntry
              value={formData.confirmPassword}
              onChangeText={(text) => setFormData({ ...formData, confirmPassword: text })}
              editable={!isLoading}
            />
          )}

          <Pressable
            style={styles.submitBtn}
            onPress={mode === 'login' ? handleLogin : handleSignup}
            disabled={isLoading}
          >
            <Text style={styles.submitTxt}>{isLoading ? 'Please wait...' : mode === 'login' ? 'Login' : 'Create Account'}</Text>
          </Pressable>

          {!!errorMessage && <Text style={styles.errorText}>{errorMessage}</Text>}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: colors.bg },
  content: { flexGrow: 1, justifyContent: 'center', padding: 22 },
  header: { alignItems: 'center', marginBottom: 20 },
  brand: { fontSize: 34, fontWeight: '900', color: colors.text },
  tagline: { color: colors.muted, fontSize: 13, marginTop: 4 },
  card: { backgroundColor: '#fff', borderColor: colors.border, borderWidth: 1, borderRadius: 18, padding: 16 },
  modeRow: { flexDirection: 'row', backgroundColor: '#EEF2F7', borderRadius: 12, padding: 4, marginBottom: 14 },
  modeBtn: { flex: 1, height: 38, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  modeBtnActive: { backgroundColor: colors.primary },
  modeTxt: { fontWeight: '700', color: '#334155' },
  modeTxtActive: { color: '#fff' },
  input: { height: 46, borderRadius: 12, borderColor: colors.border, borderWidth: 1, paddingHorizontal: 12, backgroundColor: '#F8FAFC', color: colors.text, marginBottom: 10 },
  submitBtn: { marginTop: 8, height: 46, borderRadius: 12, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center' },
  submitTxt: { color: '#fff', fontWeight: '800', fontSize: 15 },
  errorText: { marginTop: 10, color: colors.danger, fontSize: 13, textAlign: 'center' },
});
