import React from 'react';
import clsx from 'clsx';

import styles from './component.module.scss';

export type ICardVariants = 'full-width' | 'padding-sm' | 'padding-m';

export type ICardProps<Variants = ''> = {
  variants?: (ICardVariants | Variants)[];
  className?: string | { root?: string; header?: string; body?: string };
  href?: string;
  header?: React.ReactNode;
  children?: React.ReactNode;
};

export const Card = ({
  variants,
  className,
  href,
  header,
  children
}: ICardProps) => {
  const RenderComponent = href ? 'a' : 'div';

  return (
    <div
      className={clsx(
        styles.main,
        variants?.map((variant) => styles[variant]),
        typeof className === 'object' && className.root
      )}
    >
      {header && (
        <div
          className={clsx(
            styles.header,
            variants?.map((variant) => styles[variant]),
            typeof className === 'object' && className.header
          )}
        >
          {header}
        </div>
      )}
      <RenderComponent
        className={clsx(
          styles.body,
          variants?.map((variant) => styles[variant]),
          className,
          typeof className === 'object' && className.body
        )}
        href={href}
      >
        {children}
      </RenderComponent>
    </div>
  );
};
