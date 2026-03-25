import { MD3DarkTheme } from 'react-native-paper';

export const paperTheme = {
  ...MD3DarkTheme,
  roundness: 20,
  colors: {
    ...MD3DarkTheme.colors,
    primary: '#E58A3A',
    onPrimary: '#1F1308',
    secondary: '#C97830',
    onSecondary: '#1F1207',
    background: '#151515',
    onBackground: '#F2F2F2',
    surface: '#1C1C1C',
    onSurface: '#EEEEEE',
    surfaceVariant: '#2A2A2A',
    onSurfaceVariant: '#ACACAC',
    outline: '#3A3A3A',
    error: '#FF7373',
    elevation: {
      level0: 'transparent',
      level1: '#202020',
      level2: '#252525',
      level3: '#2A2A2A',
      level4: '#2F2F2F',
      level5: '#353535',
    },
  },
};
