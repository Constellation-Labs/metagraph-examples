import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider, createBrowserRouter } from 'react-router-dom';
import { initializeDayJs } from 'exlib/dayjs.ts';

import { AppLayout } from './layout.tsx';
import reportWebVitals from './reportWebVitals.ts';
import { createProviderStack } from './common/components/index.ts';
import { ApiProvider } from './common/providers/index.ts';
import { routes } from './routes.tsx';

import '@/styles/globals.scss';

initializeDayJs();

const AppCoreProviderStack = createProviderStack();
AppCoreProviderStack.addProvider(ApiProvider, {});

const AppRoutedProviderStack = createProviderStack();

const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <AppRoutedProviderStack>
        <AppLayout />
      </AppRoutedProviderStack>
    ),
    /* errorElement: <RuntimeErrorView />, */
    children: routes
  }
]);

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <React.StrictMode>
    <AppCoreProviderStack>
      <RouterProvider router={router} />
    </AppCoreProviderStack>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
