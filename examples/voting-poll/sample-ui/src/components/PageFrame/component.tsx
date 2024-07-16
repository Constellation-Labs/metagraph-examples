import clsx from 'clsx';
import React from 'react';

import { Footer } from '../Footer/component.tsx';

import styles from './component.module.scss';

export type IPageFrameProps = {
  variants?: ('noTopMargin' | 'noSidebarMargin')[];
  className?: string | { root?: string; content?: string };
  children?: React.ReactNode;
};

export const PageFrame = ({
  variants,
  className,
  children
}: IPageFrameProps) => {
  return (
    <main
      className={clsx(
        styles.main,
        variants?.map((variant) => styles[variant]),
        typeof className === 'object' && className.root
      )}
    >
      <div
        className={clsx(
          styles.content,
          variants?.map((variant) => styles[variant]),
          className,
          typeof className === 'object' && className.content
        )}
      >
        {children}
        <Footer />
      </div>
    </main>
  );
};
