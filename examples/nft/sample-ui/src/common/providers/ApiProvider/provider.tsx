import axios from 'axios';
import React from 'react';

import { EnvironmentContext } from '@/lib/index.ts';

import { IApiProviderContext } from './types.ts';

const ApiProviderContext = React.createContext<IApiProviderContext | null>(
  null
);

const ApiProvider = ({ children }: { children: React.ReactNode }) => {
  const api = axios.create({
    baseURL: EnvironmentContext.baseApiUrl
  });

  const providerContext: IApiProviderContext = {
    api
  };

  return (
    <ApiProviderContext.Provider value={providerContext}>
      {children}
    </ApiProviderContext.Provider>
  );
};

export { ApiProvider, ApiProviderContext };
