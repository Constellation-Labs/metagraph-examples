'use client';

import Blockies from 'react-blockies';

import ConstellationLogo from '../../assets/logos/constellation.svg';
import { useWalletProvider } from '../../providers';
import { shorten } from '../../utils';
import { Button } from '../Button/component';

import styles from './component.module.scss';

export const Header = () => {
  const { wallet } = useWalletProvider();

  return (
    <div className={styles.main}>
      <ConstellationLogo width={90} height={90} />
      <span>Voting Poll Example</span>
      <Button
        variants={['secondary', 'outline']}
        onClick={async () => {
          if (!wallet.active) {
            wallet.activate();
          } else {
            wallet.deactivate();
          }
        }}
        leftIcon={
          wallet.active ? (
            <Blockies
              seed={wallet.account}
              size={10}
              scale={2}
              className={styles.identicon}
            />
          ) : undefined
        }
      >
        {wallet.active ? shorten(wallet.account) : 'Connect wallet'}
      </Button>
    </div>
  );
};
