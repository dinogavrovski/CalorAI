import { withLayoutContext } from 'expo-router';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import { Home, List, TrendingUp } from 'lucide-react-native';
import { Platform, Pressable, StyleSheet, useWindowDimensions } from 'react-native';
import { useState } from 'react';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { paperTheme } from '@/constants/paperTheme';
import BottomSheetMealLogger from '@/components/BottomSheetMealLogger';
import { Plus } from 'lucide-react-native';

const TopTab = createMaterialTopTabNavigator();
const SwipeTabs = withLayoutContext(TopTab.Navigator);

export default function TabsLayout() {
  const insets = useSafeAreaInsets();
  const { width } = useWindowDimensions();
  const [mealLoggerVisible, setMealLoggerVisible] = useState(false);
  const bottomInset = insets.bottom > 0 ? insets.bottom : Platform.OS === 'android' ? 14 : 10;
  const tabBarHeight = 68;
  const tabBarWidth = Math.min(width - 120, 430);

  return (
    <>
      <SwipeTabs
        screenOptions={{
          headerShown: false,
          tabBarPosition: 'bottom',
          swipeEnabled: true,
          animationEnabled: false,
          lazy: true,
          sceneStyle: {
            backgroundColor: paperTheme.colors.background,
          },
          tabBarStyle: {
            position: 'absolute',
            left: 16,
            bottom: bottomInset + 4,
            width: tabBarWidth,
            borderRadius: 22,
            backgroundColor: '#242424',
            borderTopColor: '#353535',
            borderTopWidth: 1,
            paddingBottom: 5,
            paddingTop: 5,
            height: tabBarHeight,
            overflow: 'hidden',
            elevation: 12,
          },
          tabBarIndicatorStyle: {
            backgroundColor: 'transparent',
          },
          tabBarPressColor: 'transparent',
          tabBarShowIcon: true,
          tabBarActiveTintColor: paperTheme.colors.primary,
          tabBarInactiveTintColor: '#8A8A8A',
          tabBarItemStyle: {
            borderRadius: 14,
            marginHorizontal: 4,
            marginVertical: 1,
            paddingVertical: 1,
            justifyContent: 'center',
            alignItems: 'center',
          },
          tabBarIconStyle: {},
          tabBarLabelStyle: {
            fontSize: 11,
            lineHeight: 14,
            fontWeight: '600',
            marginBottom: 0,
            marginTop: 1,
          },
        }}
      >
        <SwipeTabs.Screen
          name="index"
          options={{
            title: 'Home',
            tabBarIcon: ({ color, size }) => (
              <Home color={color} size={size} />
            ),
          }}
        />
        <SwipeTabs.Screen
          name="logs"
          options={{
            title: 'Logs',
            tabBarIcon: ({ color, size }) => (
              <List color={color} size={size} />
            ),
          }}
        />
        <SwipeTabs.Screen
          name="progress"
          options={{
            title: 'Progress',
            tabBarIcon: ({ color, size }) => (
              <TrendingUp color={color} size={size} />
            ),
          }}
        />
      </SwipeTabs>

      <Pressable style={[styles.fab, { bottom: bottomInset + 4 }]} onPress={() => setMealLoggerVisible(true)}>
        <Plus size={26} color="#E58A3A" />
      </Pressable>

      <BottomSheetMealLogger
        isVisible={mealLoggerVisible}
        onClose={() => setMealLoggerVisible(false)}
        onSuccess={() => {}}
      />
    </>
  );
}

const styles = StyleSheet.create({
  fab: {
    position: 'absolute',
    right: 24,
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#262626',
    borderWidth: 1,
    borderColor: '#383838',
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 10,
  },
});
