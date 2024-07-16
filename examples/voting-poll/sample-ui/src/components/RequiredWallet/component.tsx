'use client';

import React from 'react';

import { useWalletProvider } from '../../providers';
import { Button } from '../Button/component';

import styles from './component.module.scss';

export type IRequiredWalletProps = { children?: React.ReactNode };

export const RequiredWallet = ({ children }: IRequiredWalletProps) => {
  const { wallet } = useWalletProvider();

  if (!wallet.active) {
    return (
      <section className={styles.noWallet}>
        <span>You need to connect your wallet to visit this section</span>
        <Button
          variants={['primary']}
          onClick={async () => {
            wallet.activate();
          }}
        >
          Connect wallet
        </Button>
      </section>
    );
  }

  return children;
};
