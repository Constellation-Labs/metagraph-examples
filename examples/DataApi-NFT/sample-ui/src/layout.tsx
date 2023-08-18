import { Outlet } from 'react-router-dom';

import {
  DefaultMantineColor,
  MantineProvider,
  MantineThemeOverride,
  Tuple
} from '@mantine/core';

import { BaseLayout } from './common/layouts/index.ts';

type AppColorNames = 'fireorange' | 'white' | 'black' | DefaultMantineColor;

declare module '@mantine/core' {
  export interface MantineThemeColorsOverride {
    colors: Record<AppColorNames, Tuple<string, 10>>;
  }
}

const AppTheme: MantineThemeOverride = {
  colors: {
    white: new Array(10).fill('#ffffff') as Tuple<string, 10>,
    black: new Array(10).fill('#000000') as Tuple<string, 10>,
    fireorange: [
      '#ffecdc',
      '#ffccaf',
      '#ffab7e',
      '#7094ff',
      '#ff691a',
      '#e65000',
      '#b43d00',
      '#812b00',
      '#4f1900',
      '#210600'
    ]
  },
  primaryColor: 'fireorange',
  fontFamily: "'Share Tech Mono', monospace",
  globalStyles: (theme) => ({
    body: {
      backgroundColor: '#1B1E29',
      color: theme.colors.fireorange[3]
    }
  })
};

const AppLayout = () => {
  return (
    <MantineProvider withGlobalStyles withNormalizeCSS theme={AppTheme}>
      <BaseLayout>
        <Outlet />
      </BaseLayout>
    </MantineProvider>
  );
};

export { AppLayout };
