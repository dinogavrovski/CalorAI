import React, { useState, useRef, useEffect } from 'react';
import {
  Modal,
  View,
  Text,
  TextInput,
  Pressable,
  StyleSheet,
  Animated,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { X, Send } from 'lucide-react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors } from '@/constants/uiTheme';
import apiService from '@/services/api';
import { Alert } from 'react-native';

interface BottomSheetMealLoggerProps {
  isVisible: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function BottomSheetMealLogger({
  isVisible,
  onClose,
  onSuccess,
}: BottomSheetMealLoggerProps) {
  const [mealInput, setMealInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const fadeAnim = useRef(new Animated.Value(0)).current;
  const inputRef = useRef<TextInput>(null);

  useEffect(() => {
    if (isVisible) {
      setMealInput('');
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 300,
        useNativeDriver: true,
      }).start();
      setTimeout(() => inputRef.current?.focus(), 200);
    } else {
      Animated.timing(fadeAnim, {
        toValue: 0,
        duration: 200,
        useNativeDriver: true,
      }).start();
    }
  }, [isVisible]);

  const handleLogMeal = async () => {
    if (!mealInput.trim()) {
      Alert.alert('Error', 'Please enter a meal description');
      return;
    }

    setIsLoading(true);
    try {
      const estimate = await apiService.logFoodText(mealInput);
      const savedMeal = await apiService.saveMealHistory(estimate);
      Alert.alert('Success', `Saved ${Math.round(savedMeal.total_calories)} kcal`);
      setMealInput('');
      onClose();
      onSuccess();
    } catch {
      Alert.alert('Error', 'Could not log meal');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal visible={isVisible} transparent animationType="none">
      <Animated.View style={[styles.container, { opacity: fadeAnim }]}>
        <SafeAreaView style={styles.safeArea} edges={['top', 'left', 'right']}>
          <KeyboardAvoidingView
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
            style={styles.inner}
          >
            {/* Header with close button */}
            <View style={styles.header}>
              <Text style={styles.title}>Log a Meal</Text>
              <Pressable onPress={onClose} hitSlop={8}>
                <X size={24} color={colors.text} />
              </Pressable>
            </View>

            {/* Main content area - centered input focus */}
            <View style={styles.content}>
              <Text style={styles.subtitle}>What did you eat?</Text>
              
              <TextInput
                ref={inputRef}
                style={styles.input}
                placeholder="grilled chicken with rice and broccoli"
                placeholderTextColor={colors.muted}
                value={mealInput}
                onChangeText={setMealInput}
                multiline
                editable={!isLoading}
                maxLength={500}
              />

              <Text style={styles.counter}>
                {mealInput.length}/500
              </Text>
            </View>

            {/* Action buttons - above keyboard */}
            <View style={styles.actions}>
              <Pressable
                style={[styles.btnCancel]}
                onPress={onClose}
                disabled={isLoading}
              >
                <Text style={styles.btnCancelText}>Cancel</Text>
              </Pressable>
              <Pressable
                style={[
                  styles.btnLog,
                  isLoading && styles.btnLogDisabled,
                ]}
                onPress={handleLogMeal}
                disabled={isLoading || !mealInput.trim()}
              >
                <Send size={16} color="#fff" />
                <Text style={styles.btnLogText}>
                  {isLoading ? 'Logging...' : 'Log'}
                </Text>
              </Pressable>
            </View>
          </KeyboardAvoidingView>
        </SafeAreaView>
      </Animated.View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  safeArea: {
    flex: 1,
  },
  inner: {
    flex: 1,
    display: 'flex',
    justifyContent: 'space-between',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 8,
    paddingBottom: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: '900',
    color: colors.text,
    letterSpacing: -0.5,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  subtitle: {
    color: colors.muted,
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 20,
    textAlign: 'center',
  },
  input: {
    backgroundColor: '#FCFAF7',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: colors.border,
    color: colors.text,
    fontSize: 18,
    paddingVertical: 18,
    paddingHorizontal: 18,
    minHeight: 120,
    maxHeight: 280,
    textAlignVertical: 'center',
    marginBottom: 12,
  },
  counter: {
    fontSize: 12,
    color: colors.muted,
    fontWeight: '600',
    textAlign: 'center',
  },
  actions: {
    flexDirection: 'row',
    gap: 10,
    paddingHorizontal: 20,
    paddingBottom: 20,
  },
  btnCancel: {
    flex: 1,
    height: 50,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFF7EF',
  },
  btnCancelText: {
    color: colors.text,
    fontWeight: '700',
    fontSize: 16,
  },
  btnLog: {
    flex: 1,
    height: 50,
    borderRadius: 14,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 8,
  },
  btnLogDisabled: {
    opacity: 0.6,
  },
  btnLogText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 16,
  },
});
