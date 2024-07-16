'use client';

import styles from './component.module.scss';

export const Footer = () => {
  return (
    <div className={styles.main}>
      <span>© {new Date().getFullYear()} Constellation Network</span>
    </div>
  );
};
