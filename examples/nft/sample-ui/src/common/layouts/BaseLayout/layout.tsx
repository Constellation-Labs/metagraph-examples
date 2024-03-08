import React from 'react';

import { Header } from '../../components/index.ts';

import styles from './layout.module.scss';

const BaseLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className={styles.main}>
      <Header></Header>
      <div className={styles.content}>{children}</div>
    </div>
  );
};

export { BaseLayout };
