'use client';

import clsx from 'clsx';
import { ImpulseSpinner } from 'react-spinners-kit';
import React, { ComponentProps } from 'react';
import Link from 'next/link';

import { IButtonProps } from '../component';
import styles from '../component.module.scss';

export type IButtonLinkProps<Variants = ''> = ComponentProps<typeof Link> &
  IButtonProps<Variants>;

export const ButtonLink = React.forwardRef<HTMLAnchorElement, IButtonLinkProps>(
  (
    { loading, leftIcon, rightIcon, variants, className, children, ...props },
    ref
  ) => {
    return (
      <Link
        {...props}
        href={props.disabled ? '' : props.href}
        className={clsx(
          styles.main,
          variants?.map((variant) => styles[variant || '']),
          className
        )}
        ref={ref}
      >
        {leftIcon && (
          <span className={clsx(styles.icon, styles.left)}>{leftIcon}</span>
        )}
        {loading ? <ImpulseSpinner /> : children}
        {rightIcon && (
          <span className={clsx(styles.icon, styles.right)}>{rightIcon}</span>
        )}
      </Link>
    );
  }
);

ButtonLink.displayName = 'ButtonLink';
