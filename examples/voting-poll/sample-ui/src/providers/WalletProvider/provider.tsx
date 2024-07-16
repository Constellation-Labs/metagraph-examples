'use client';

import { useStargazerWallet } from '@stardust-collective/web3-react-stargazer-connector';
import { createContext, useContext, useEffect, useState } from 'react';
import Cookies from 'js-cookie';

import { loginWallet, logoutWallet, requestAddressBalance } from './actions';
import { WALLET_COOKIE_NAME } from './consts';

type IWalletProviderContext = {
  wallet: ReturnType<typeof useStargazerWallet>;
  walletBalance: number | null;
  requestDataSignature: (
    payload: Record<string, any>
  ) => Promise<{ pub: string; signature: string; payload: string }>;
};

const WalletProviderContext = createContext<IWalletProviderContext | null>(
  null
);

export const WalletProvider = ({
  children
}: {
  children?: React.ReactNode;
}) => {
  const wallet = useStargazerWallet();

  const [walletBalance, setWalletBalance] = useState<number | null>(null);

  const addressCookie = Cookies.get(WALLET_COOKIE_NAME);

  useEffect(() => {
    if (wallet.active && !addressCookie) {
      loginWallet(wallet.account);
    }

    if (!wallet.active && addressCookie) {
      logoutWallet();
    }
  }, [wallet.active && wallet.account]);

  useEffect(() => {
    if (wallet.active) {
      (async () => {
        setWalletBalance(await requestAddressBalance(wallet.account));
      })();
    }
  }, [wallet.active && wallet.account]);

  useEffect(() => {
    if (addressCookie) {
      wallet.activate();
    }
  }, [addressCookie]);

  const requestDataSignature: IWalletProviderContext['requestDataSignature'] =
    async (payload) => {
      if (!wallet.active) {
        throw new Error('Wallet is not active, cannot sign messages');
      }

      const payloadEncoded = btoa(JSON.stringify(payload));

      const signature = await wallet.request({
        method: 'dag_signData',
        params: [wallet.account, payloadEncoded]
      });

      const pub = await wallet.request({
        method: 'dag_getPublicKey',
        params: [wallet.account]
      });

      return { pub, signature, payload: payloadEncoded };
    };

  return (
    <WalletProviderContext.Provider
      value={{ wallet, walletBalance, requestDataSignature }}
    >
      {children}
    </WalletProviderContext.Provider>
  );
};

export const useWalletProvider = () => {
  const ctx = useContext(WalletProviderContext);

  if (!ctx) {
    throw new Error(
      'useWalletProvider calls must be done under a <WalletProvider/> component'
    );
  }

  return ctx;
};
