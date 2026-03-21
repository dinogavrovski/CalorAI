import React, { useState } from 'react';
import {
  View,
  StyleSheet,
  Alert,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import {
  TextInput,
  Button,
  Text,
  Surface,
  HelperText,
  TouchableRipple,
} from 'react-native-paper';
import { useAuth } from '../context/AuthContext';
import { AntDesign } from '@expo/vector-icons';

export const AuthScreen = () => {
  const { login, register, isLoading } = useAuth();
  const [isLoginMode, setIsLoginMode] = useState(true);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<any>({});

  const validate = () => {
    const newErrors: any = {};

    if (!username.trim()) {
      newErrors.username = 'Username is required';
    }

    if (!isLoginMode && !email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!isLoginMode && !email.includes('@')) {
      newErrors.email = 'Enter a valid email';
    }

    if (!password.trim()) {
      newErrors.password = 'Password is required';
    } else if (password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    if (!isLoginMode && password !== confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAuth = async () => {
    if (!validate()) return;

    try {
      if (isLoginMode) {
        await login(username, password);
      } else {
        await register(username, email, password);
      }
    } catch (error: any) {
      Alert.alert(
        'Auth Error',
        error.response?.data?.detail || 'Authentication failed'
      );
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={styles.container}
    >
      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Logo / Header */}
        <View style={styles.logoContainer}>
          <AntDesign name="apple" size={48} color="#0066cc" />
          <Text style={styles.logoText}>CalAI</Text>
          <Text style={styles.subtitle}>Smart Meal Logging</Text>
        </View>

        {/* Auth Mode Toggle */}
        <Surface style={styles.modeToggle}>
          <View style={styles.toggleButtonGroup}>
            <TouchableRipple
              style={[
                styles.toggleButton,
                isLoginMode && styles.toggleButtonActive,
              ]}
              onPress={() => {
                setIsLoginMode(true);
                setErrors({});
              }}
            >
              <Text style={isLoginMode ? styles.toggleTextActive : styles.toggleText}>
                Login
              </Text>
            </TouchableRipple>
            <TouchableRipple
              style={[
                styles.toggleButton,
                !isLoginMode && styles.toggleButtonActive,
              ]}
              onPress={() => {
                setIsLoginMode(false);
                setErrors({});
              }}
            >
              <Text style={!isLoginMode ? styles.toggleTextActive : styles.toggleText}>
                Register
              </Text>
            </TouchableRipple>
          </View>
        </Surface>

        {/* Form */}
        <Surface style={styles.formContainer}>
          {/* Username */}
          <TextInput
            label="Username"
            value={username}
            onChangeText={setUsername}
            mode="outlined"
            style={styles.input}
            editable={!isLoading}
            error={!!errors.username}
          />
          {errors.username && (
            <HelperText type="error" visible={!!errors.username}>
              {errors.username}
            </HelperText>
          )}

          {/* Email (Register Only) */}
          {!isLoginMode && (
            <>
              <TextInput
                label="Email"
                value={email}
                onChangeText={setEmail}
                mode="outlined"
                style={styles.input}
                keyboardType="email-address"
                editable={!isLoading}
                error={!!errors.email}
              />
              {errors.email && (
                <HelperText type="error" visible={!!errors.email}>
                  {errors.email}
                </HelperText>
              )}
            </>
          )}

          {/* Password */}
          <TextInput
            label="Password"
            value={password}
            onChangeText={setPassword}
            mode="outlined"
            style={styles.input}
            secureTextEntry
            editable={!isLoading}
            error={!!errors.password}
          />
          {errors.password && (
            <HelperText type="error" visible={!!errors.password}>
              {errors.password}
            </HelperText>
          )}

          {/* Confirm Password (Register Only) */}
          {!isLoginMode && (
            <>
              <TextInput
                label="Confirm Password"
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                mode="outlined"
                style={styles.input}
                secureTextEntry
                editable={!isLoading}
                error={!!errors.confirmPassword}
              />
              {errors.confirmPassword && (
                <HelperText type="error" visible={!!errors.confirmPassword}>
                  {errors.confirmPassword}
                </HelperText>
              )}
            </>
          )}

          {/* Submit Button */}
          <Button
            mode="contained"
            onPress={handleAuth}
            loading={isLoading}
            disabled={isLoading}
            style={styles.submitButton}
          >
            {isLoginMode ? 'Login' : 'Create Account'}
          </Button>
        </Surface>

        {/* Tips */}
        <View style={styles.tipsContainer}>
          <Text style={styles.tipsTitle}>
            {isLoginMode ? '💡 Demo Tip' : '📝 Getting Started'}
          </Text>
          <Text style={styles.tipsText}>
            {isLoginMode
              ? 'Use any username and password to log in (backend demo mode)'
              : 'Create an account to start logging meals and tracking calories'}
          </Text>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: 16,
    paddingVertical: 32,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 32,
  },
  logoText: {
    fontSize: 32,
    fontWeight: '700',
    color: '#0066cc',
    marginTop: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#999',
    marginTop: 4,
  },
  modeToggle: {
    marginBottom: 20,
    padding: 8,
    borderRadius: 12,
  },
  formContainer: {
    padding: 20,
    borderRadius: 12,
    marginBottom: 20,
  },
  input: {
    marginBottom: 8,
  },
  submitButton: {
    paddingVertical: 8,
    marginTop: 12,
  },
  tipsContainer: {
    padding: 16,
    backgroundColor: '#e8f4fd',
    borderRadius: 12,
    borderLeftWidth: 4,
    borderLeftColor: '#0066cc',
  },
  tipsTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#0066cc',
    marginBottom: 8,
  },
  tipsText: {
    fontSize: 12,
    color: '#666',
    lineHeight: 18,
  },
  toggleButtonGroup: {
    flexDirection: 'row',
    borderRadius: 8,
    overflow: 'hidden',
    backgroundColor: '#f0f0f0',
  },
  toggleButton: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 16,
    alignItems: 'center',
    borderRadius: 8,
  },
  toggleButtonActive: {
    backgroundColor: '#0066cc',
  },
  toggleText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#999',
  },
  toggleTextActive: {
    fontSize: 14,
    fontWeight: '600',
    color: '#fff',
  },
});
