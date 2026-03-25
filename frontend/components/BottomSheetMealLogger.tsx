import React, { useState, useRef, useEffect } from 'react';
import {
  Modal,
  View,
  Text,
  TextInput,
  Pressable,
  StyleSheet,
  Animated,
  Keyboard,
  Easing,
  Platform,
} from 'react-native';
import { X, Send } from 'lucide-react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import { paperTheme } from '@/constants/paperTheme';
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
  const insets = useSafeAreaInsets();
  const [mealInput, setMealInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isMounted, setIsMounted] = useState(isVisible);
  const fadeAnim = useRef(new Animated.Value(0)).current;
  const slideAnim = useRef(new Animated.Value(28)).current;
  const keyboardOffset = useRef(new Animated.Value(0)).current;
  const inputRef = useRef<TextInput>(null);

  useEffect(() => {
    if (isVisible) {
      setIsMounted(true);
      setMealInput('');
      fadeAnim.setValue(0);
      slideAnim.setValue(28);

      Animated.parallel([
        Animated.timing(fadeAnim, {
          toValue: 1,
          duration: 260,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: false,
        }),
        Animated.timing(slideAnim, {
          toValue: 0,
          duration: 320,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: false,
        }),
      ]).start(() => {
        setTimeout(() => inputRef.current?.focus(), 80);
      });
    } else {
      if (!isMounted) {
        return;
      }

      Animated.parallel([
        Animated.timing(fadeAnim, {
          toValue: 0,
          duration: 180,
          easing: Easing.in(Easing.cubic),
          useNativeDriver: false,
        }),
        Animated.timing(slideAnim, {
          toValue: 18,
          duration: 200,
          easing: Easing.in(Easing.cubic),
          useNativeDriver: false,
        }),
      ]).start(() => {
        setIsMounted(false);
      });
    }
  }, [isVisible, isMounted, fadeAnim, slideAnim]);

  useEffect(() => {
    const showEvent = Platform.OS === 'ios' ? 'keyboardWillShow' : 'keyboardDidShow';
    const hideEvent = Platform.OS === 'ios' ? 'keyboardWillHide' : 'keyboardDidHide';

    const showSub = Keyboard.addListener(showEvent, (event) => {
      const duration = event.duration ?? 250;
      Animated.timing(keyboardOffset, {
        toValue: event.endCoordinates.height,
        duration,
        easing: Easing.out(Easing.cubic),
        useNativeDriver: false,
      }).start();
    });

    const hideSub = Keyboard.addListener(hideEvent, (event) => {
      const duration = event?.duration ?? 220;
      Animated.timing(keyboardOffset, {
        toValue: 0,
        duration,
        easing: Easing.out(Easing.cubic),
        useNativeDriver: false,
      }).start();
    });

    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, [keyboardOffset]);

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
    <Modal visible={isMounted} animationType="none" transparent>
      <Animated.View
        style={[
          styles.container,
          {
            opacity: fadeAnim,
            transform: [{ translateY: slideAnim }],
          },
        ]}
      >
        <SafeAreaView style={styles.safeArea} edges={['top', 'left', 'right']}>
          <View style={styles.inner}>
            {/* Header with close button */}
            <View style={styles.header}>
              <Text style={styles.title}>Log a Meal</Text>
              <Pressable onPress={onClose} hitSlop={8}>
                <X size={24} color={paperTheme.colors.onSurface} />
              </Pressable>
            </View>

            {/* Main content area - centered input focus */}
            <View style={styles.content}>
              <Text style={styles.subtitle}>What did you eat?</Text>
              
              <TextInput
                ref={inputRef}
                style={styles.input}
                placeholder="grilled chicken with rice and broccoli"
                placeholderTextColor={paperTheme.colors.onSurfaceVariant}
                value={mealInput}
                onChangeText={setMealInput}
                multiline
                editable={!isLoading}
                maxLength={500}
              />

              <Text style={styles.tipText}>
                Tip: Be specific with ingredients and portions for better calorie estimates.
              </Text>
            </View>

            {/* Action buttons - above keyboard */}
            <Animated.View
              style={[
                styles.actions,
                {
                  transform: [{ translateY: Animated.multiply(keyboardOffset, -1) }],
                  paddingBottom: insets.bottom + 12,
                },
              ]}
            >
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
                <Send size={16} color={paperTheme.colors.onPrimary} />
                <Text style={styles.btnLogText}>
                  {isLoading ? 'Logging...' : 'Log'}
                </Text>
              </Pressable>
            </Animated.View>
          </View>
        </SafeAreaView>
      </Animated.View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: paperTheme.colors.background,
  },
  safeArea: {
    flex: 1,
  },
  inner: {
    flex: 1,
    justifyContent: 'flex-start',
    backgroundColor: paperTheme.colors.background,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 10,
    paddingBottom: 14,
  },
  title: {
    fontSize: 24,
    lineHeight: 34,
    fontWeight: '600',
    color: paperTheme.colors.onSurface,
  },
  content: {
    paddingHorizontal: 20,
    paddingTop: 4,
  },
  subtitle: {
    color: paperTheme.colors.onSurfaceVariant,
    fontSize: 13,
    fontWeight: '500',
    marginBottom: 20,
    textAlign: 'left',
  },
  input: {
    backgroundColor: paperTheme.colors.surface,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: paperTheme.colors.outline,
    color: paperTheme.colors.onSurface,
    fontSize: 16,
    paddingVertical: 18,
    paddingHorizontal: 18,
    minHeight: 140,
    maxHeight: 280,
    textAlignVertical: 'top',
    marginBottom: 12,
  },
  tipText: {
    marginTop: 2,
    color: paperTheme.colors.onSurfaceVariant,
    fontSize: 12,
    fontWeight: '500',
    textAlign: 'left',
  },
  actions: {
    position: 'absolute',
    left: 20,
    right: 20,
    bottom: 0,
    flexDirection: 'row',
    gap: 10,
  },
  btnCancel: {
    flex: 1,
    height: 50,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: paperTheme.colors.outline,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: paperTheme.colors.surfaceVariant,
  },
  btnCancelText: {
    color: paperTheme.colors.onSurface,
    fontWeight: '700',
    fontSize: 16,
  },
  btnLog: {
    flex: 1,
    height: 50,
    borderRadius: 14,
    backgroundColor: paperTheme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 8,
  },
  btnLogDisabled: {
    opacity: 0.6,
  },
  btnLogText: {
    color: paperTheme.colors.onPrimary,
    fontWeight: '700',
    fontSize: 16,
  },
});
