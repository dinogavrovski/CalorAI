import React, { useEffect, useRef, useState } from 'react';
import {
  Animated,
  Alert,
  Keyboard,
  KeyboardEvent,
  KeyboardAvoidingView,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import * as WebBrowser from 'expo-web-browser';
import * as Google from 'expo-auth-session/providers/google';
import { useRouter } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import { colors } from '@/constants/uiTheme';
import { API_CONFIG } from '@/constants/config';

type AuthMode = 'login' | 'signup';

WebBrowser.maybeCompleteAuthSession();

export default function AuthScreen() {
  const { login, loginWithGoogle, register, isLoading } = useAuth();
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const scrollRef = useRef<ScrollView>(null);
  const modeFade = useRef(new Animated.Value(1)).current;
  const keyboardHeight = useRef(new Animated.Value(0)).current;
  const [isKeyboardVisible, setIsKeyboardVisible] = useState(false);
  const [mode, setMode] = useState<AuthMode>('login');
  const [errorMessage, setErrorMessage] = useState('');

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [request, response, promptAsync] = Google.useIdTokenAuthRequest({
    androidClientId: API_CONFIG.GOOGLE.ANDROID_CLIENT_ID,
    webClientId: API_CONFIG.GOOGLE.WEB_CLIENT_ID,
    expoClientId: API_CONFIG.GOOGLE.EXPO_CLIENT_ID,
  });

  useEffect(() => {
    const onKeyboardShow = (event: KeyboardEvent) => {
      setIsKeyboardVisible(true);
      Animated.timing(keyboardHeight, {
        toValue: event.endCoordinates.height,
        duration: 180,
        useNativeDriver: false,
      }).start();
    };

    const onKeyboardHide = () => {
      setIsKeyboardVisible(false);
      Animated.timing(keyboardHeight, {
        toValue: 0,
        duration: 180,
        useNativeDriver: false,
      }).start();
    };

    const showSub = Keyboard.addListener('keyboardDidShow', onKeyboardShow);
    const hideSub = Keyboard.addListener('keyboardDidHide', onKeyboardHide);

    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, [keyboardHeight]);

  useEffect(() => {
    const processGoogleResponse = async () => {
      if (response?.type !== 'success') return;

      const idToken = response.params?.id_token;
      if (!idToken) {
        Alert.alert('Google Sign In', 'Google token was not returned.');
        return;
      }

      try {
        await loginWithGoogle(idToken);
        router.replace('/(tabs)');
      } catch (error: any) {
        const message = error?.response?.data?.detail || 'Google sign in failed.';
        Alert.alert('Google Sign In', message);
        setErrorMessage(message);
      }
    };

    processGoogleResponse();
  }, [loginWithGoogle, response, router]);

  const switchMode = (nextMode: AuthMode) => {
    if (nextMode === mode) return;

    Animated.timing(modeFade, {
      toValue: 0,
      duration: 120,
      useNativeDriver: true,
    }).start(() => {
      setMode(nextMode);
      Animated.timing(modeFade, {
        toValue: 1,
        duration: 180,
        useNativeDriver: true,
      }).start();

      if (nextMode === 'signup' && isKeyboardVisible) {
        setTimeout(() => {
          scrollRef.current?.scrollTo({ y: 130, animated: true });
        }, 120);
      }
    });
  };

  const handleFocusField = (index: number) => {
    setTimeout(() => {
      // Nudge only lower fields into view while keeping layout stable.
      const targetY = index <= 1 ? 0 : index === 2 ? 105 : 170;
      scrollRef.current?.scrollTo({ y: targetY, animated: true });
    }, 80);
  };

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

  const handleGoogleSignIn = async () => {
    const hasPlaceholderId =
      API_CONFIG.GOOGLE.ANDROID_CLIENT_ID.startsWith('YOUR_') ||
      API_CONFIG.GOOGLE.EXPO_CLIENT_ID.startsWith('YOUR_') ||
      API_CONFIG.GOOGLE.WEB_CLIENT_ID.startsWith('YOUR_');

    if (hasPlaceholderId) {
      Alert.alert(
        'Google Auth Not Configured',
        'Set Google client IDs in frontend/constants/config.ts first.'
      );
      return;
    }

    await promptAsync();
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
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={undefined}
        keyboardVerticalOffset={0}
      >
        <ScrollView
          ref={scrollRef}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="on-drag"
          contentContainerStyle={[
            styles.content,
            {
              justifyContent: 'center',
              paddingBottom: 32 + insets.bottom,
            },
          ]}
        >
          <View style={styles.header}>
            <Text style={styles.brand}>CalAI</Text>
            <Text style={styles.tagline}>Smart Nutrition Tracking</Text>
          </View>

          <Animated.View
            style={[
              styles.card,
              {
                opacity: modeFade,
                transform: [
                  { translateY: modeFade.interpolate({ inputRange: [0, 1], outputRange: [8, 0] }) },
                  {
                    translateY: isKeyboardVisible && mode === 'signup'
                      ? keyboardHeight.interpolate({ inputRange: [0, 420], outputRange: [0, -58], extrapolate: 'clamp' })
                      : 0,
                  },
                ],
              },
            ]}
          >
            <Pressable
              style={[styles.googleBtn, (!request || isLoading) && styles.googleBtnDisabled]}
              onPress={handleGoogleSignIn}
              disabled={!request || isLoading}
            >
              <Text style={styles.googleBtnText}>Continue with Google</Text>
            </Pressable>

            <View style={styles.dividerRow}>
              <View style={styles.divider} />
              <Text style={styles.dividerText}>or use email</Text>
              <View style={styles.divider} />
            </View>

            <View style={styles.modeRow}>
              <Pressable style={[styles.modeBtn, mode === 'login' && styles.modeBtnActive]} onPress={() => switchMode('login')}>
                <Text style={[styles.modeTxt, mode === 'login' && styles.modeTxtActive]}>Login</Text>
              </Pressable>
              <Pressable style={[styles.modeBtn, mode === 'signup' && styles.modeBtnActive]} onPress={() => switchMode('signup')}>
                <Text style={[styles.modeTxt, mode === 'signup' && styles.modeTxtActive]}>Sign Up</Text>
              </Pressable>
            </View>

            <TextInput
              style={styles.input}
              placeholder={mode === 'login' ? 'Email' : 'Username'}
              placeholderTextColor="#94A3B8"
              value={formData.username}
              onFocus={() => handleFocusField(0)}
              onChangeText={(text) => setFormData({ ...formData, username: text })}
              editable={!isLoading}
              autoCapitalize="none"
            />

            {mode === 'signup' && (
              <TextInput
                style={styles.input}
                placeholder="Email"
                placeholderTextColor="#94A3B8"
                value={formData.email}
                keyboardType="email-address"
                onFocus={() => handleFocusField(1)}
                onChangeText={(text) => setFormData({ ...formData, email: text })}
                editable={!isLoading}
                autoCapitalize="none"
              />
            )}

            <TextInput
              style={styles.input}
              placeholder="Password"
              placeholderTextColor="#94A3B8"
              secureTextEntry
              value={formData.password}
              onFocus={() => handleFocusField(mode === 'signup' ? 2 : 1)}
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
                onFocus={() => handleFocusField(3)}
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
          </Animated.View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  root: { flex: 1, backgroundColor: colors.bg },
  content: { flexGrow: 1, justifyContent: 'center', padding: 22 },
  header: { alignItems: 'center', marginBottom: 20 },
  brand: { fontSize: 34, fontWeight: '900', color: colors.text },
  tagline: { color: colors.muted, fontSize: 13, marginTop: 4 },
  card: { backgroundColor: '#fff', borderColor: colors.border, borderWidth: 1, borderRadius: 18, padding: 16 },
  googleBtn: {
    height: 48,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#D7DDE8',
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
  },
  googleBtnDisabled: { opacity: 0.6 },
  googleBtnText: { color: '#1F2937', fontWeight: '700', fontSize: 15 },
  dividerRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  divider: { flex: 1, height: 1, backgroundColor: '#E2E8F0' },
  dividerText: { marginHorizontal: 10, color: colors.muted, fontSize: 12, fontWeight: '600' },
  modeRow: { flexDirection: 'row', backgroundColor: '#EEF2F7', borderRadius: 12, padding: 4, marginBottom: 14 },
  modeBtn: { flex: 1, height: 38, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  modeBtnActive: { backgroundColor: colors.primary, shadowColor: '#935034', shadowOpacity: 0.2, shadowRadius: 6, shadowOffset: { width: 0, height: 2 }, elevation: 2 },
  modeTxt: { fontWeight: '700', color: '#334155' },
  modeTxtActive: { color: '#fff' },
  input: { height: 46, borderRadius: 12, borderColor: colors.border, borderWidth: 1, paddingHorizontal: 12, backgroundColor: '#F8FAFC', color: colors.text, marginBottom: 10 },
  submitBtn: { marginTop: 8, height: 46, borderRadius: 12, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center' },
  submitTxt: { color: '#fff', fontWeight: '800', fontSize: 15 },
  errorText: { marginTop: 10, color: colors.danger, fontSize: 13, textAlign: 'center' },
});
