'use client';

import clsx from 'clsx';
import { ImpulseSpinner } from 'react-spinners-kit';
import React from 'react';

import styles from './component.module.scss';

export type IButtonVariants =
  | 'full-width'
  | 'text-sm'
  | 'text-m'
  | 'padding-sm'
  | 'padding-m'
  | 'centered'
  | 'primary'
  | 'secondary'
  | 'disabled'
  | 'outline'
  | 'outline-fade'
  | 'border-fade'
  | 'error'
  | false
  | null;

export type IButtonProps<Variants = ''> = JSX.IntrinsicElements['button'] & {
  loading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  variants?: (IButtonVariants | Variants)[];
};

export const Button = React.forwardRef<HTMLButtonElement, IButtonProps>(
  (
    { loading, leftIcon, rightIcon, variants, className, children, ...props },
    ref
  ) => {
    return (
      <button
        {...props}
        onClick={props.disabled ? undefined : props.onClick}
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
      </button>
    );
  }
);

Button.displayName = 'Button';
