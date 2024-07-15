'use client';

import Blockies from 'react-blockies';
import Link from 'next/link';
import { useEffect, useState } from 'react';

import ConstellationLogo from '../../assets/logos/constellation.svg';
import { useWalletProvider } from '../../providers';
import { shorten } from '../../utils';
import { Button } from '../Button/component';

import styles from './component.module.scss';
import { getMetagraphLatestSnapshot } from './actions';

export const Header = () => {
  const { wallet, walletBalance } = useWalletProvider();
  const [latestOrdinal, setLatestOrdinal] = useState(0);

  useEffect(() => {
    const iid = window.setInterval(async () => {
      const snapshot = await getMetagraphLatestSnapshot();
      setLatestOrdinal(snapshot.value.ordinal);
    }, 5 * 1000);
    return () => {
      window.clearInterval(iid);
    };
  }, []);

  return (
    <div className={styles.main}>
      <Link href="/" className={styles.constellationLogo}>
        <ConstellationLogo width={90} height={90} />
      </Link>
      <Link href="/">Voting Poll Example</Link>
      <div className={styles.buttons}>
        {latestOrdinal !== 0 && (
          <Button variants={['secondary', 'outline', 'centered']}>
            Snapshot Ordinal: {latestOrdinal}
          </Button>
        )}
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
          {wallet.active
            ? `${shorten(wallet.account)} : ${walletBalance} vp`
            : 'Connect wallet'}
        </Button>
      </div>
    </div>
  );
};
