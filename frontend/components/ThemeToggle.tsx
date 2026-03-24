import { useColorScheme, TouchableOpacity, Alert } from 'react-native';
import { Sun, Moon } from 'lucide-react-native';
import { colors } from '@/constants/uiTheme';

export function ThemeToggle() {
  const colorScheme = useColorScheme();

  return (
    <TouchableOpacity onPress={() => Alert.alert('Theme', 'Theme toggle is available in system settings.')}>
      {colorScheme === 'dark' ? (
        <Sun color={colors.text} size={22} />
      ) : (
        <Moon color={colors.text} size={22} />
      )}
    </TouchableOpacity>
  );
}
