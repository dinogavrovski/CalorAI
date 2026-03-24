import { vars } from 'nativewind';

// Keep these as RGB triplets so Tailwind alpha modifiers work.
export const lightTheme = vars({
  '--color-background': '255 255 255',
  '--color-foreground': '0 0 0',
  '--color-card': '245 245 245',
  '--color-primary': '76 175 80',
  '--color-primary-foreground': '255 255 255',
  '--color-secondary': '240 240 240',
  '--color-secondary-foreground': '0 0 0',
  '--color-muted': '224 224 224',
  '--color-muted-foreground': '102 102 102',
  '--color-border': '224 224 224',
  '--color-destructive': '239 68 68',
});

export const darkTheme = vars({
  '--color-background': '10 10 10',
  '--color-foreground': '255 255 255',
  '--color-card': '28 28 28',
  '--color-primary': '74 222 128',
  '--color-primary-foreground': '10 10 10',
  '--color-secondary': '46 46 46',
  '--color-secondary-foreground': '255 255 255',
  '--color-muted': '64 64 64',
  '--color-muted-foreground': '160 160 160',
  '--color-border': '51 51 51',
  '--color-destructive': '239 68 68',
});
