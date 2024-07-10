'use client';

import ConstellationLogo from '../../assets/logos/constellation.svg';

import styles from './component.module.scss';

export const Header = () => {
  return (
    <div className={styles.main}>
      <ConstellationLogo width={90} height={90} />
      <span>Voting Poll Example</span>
      <div></div>
    </div>
  );
};
