import React, { useContext } from 'react';

const getProviderStateHookName = (providerName: string) => {
  const pattern = /(\w+)provider/i;
  const match = pattern.exec(providerName);

  if (!match) {
    return 'use' + providerName;
  }

  return `use${match[1]}`;
};

const createProviderStateHook = <HookState>(
  provider: React.FC<any>,
  context: React.Context<HookState | null>
) => {
  const providerName = provider.displayName ?? provider.name;

  const stateHookName = !providerName
    ? 'useProviderContext'
    : getProviderStateHookName(providerName);

  const useStateHook = () => {
    const ctx = useContext(context);

    if (!ctx) {
      throw new Error(
        `${stateHookName} must be used under a <${providerName}/> component`
      );
    }

    return ctx;
  };

  Object.defineProperty(useStateHook, 'name', { value: stateHookName });

  return useStateHook;
};

export { createProviderStateHook };
