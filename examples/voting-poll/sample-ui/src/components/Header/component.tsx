'use client';

import Blockies from 'react-blockies';
import Link from 'next/link';

import ConstellationLogo from '../../assets/logos/constellation.svg';
import { useWalletProvider } from '../../providers';
import { shorten } from '../../utils';
import { Button } from '../Button/component';
import { ButtonLink } from '../Button/ButtonLink/component';

import styles from './component.module.scss';

export const Header = () => {
  const { wallet } = useWalletProvider();

  return (
    <div className={styles.main}>
      <Link href="/" className={styles.constellationLogo}>
        <ConstellationLogo width={90} height={90} />
      </Link>
      <Link href="/">Voting Poll Example</Link>
      <div className={styles.buttons}>
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
        <ButtonLink
          variants={['secondary', 'outline', 'centered']}
          href={'/polls/create'}
        >
          Create Poll
        </ButtonLink>
      </div>
    </div>
  );
};
