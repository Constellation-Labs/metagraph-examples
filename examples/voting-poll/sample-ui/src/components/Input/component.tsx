'use client';

import clsx from 'clsx';
import React, { ComponentProps } from 'react';
import { Control } from 'react-hook-form';

import styles from './component.module.scss';
import { InputSelect } from './InputSelect/component';
import { InputAddOptions } from './InputAddOptions/component';

export type IInputVariants = 'full-width' | 'padding-sm' | 'padding-m';

export type IInputOption = { value: string; content?: string };

export type IInputProps<Variants = ''> = Omit<
  JSX.IntrinsicElements['input'],
  'type'
> & {
  variants?: (IInputVariants | Variants)[];
  label?: string;
  description?: string;
  staticPrefix?: React.ReactNode;
  inputExtra?: React.ReactNode;
  error?: string | null;
  type?: JSX.IntrinsicElements['input']['type'] | 'select' | 'add-options';
  options?: IInputOption[];
  control?: Control;
  rootProps?: ComponentProps<'div'>;
};

export const Input = React.forwardRef<HTMLInputElement, IInputProps>(
  (
    {
      variants,
      label,
      description,
      staticPrefix,
      inputExtra,
      error,
      rootProps,
      ...props
    },
    ref
  ) => {
    return (
      <div
        className={clsx(
          styles.main,
          error && styles.error,
          variants?.map((variant) => styles[variant])
        )}
        {...rootProps}
      >
        {(label || description) && (
          <div className={styles.labelWrapper}>
            {label && <span className={styles.label}>{label}</span>}
            {description && (
              <span className={styles.description}>{description}</span>
            )}
          </div>
        )}
        <div className={clsx(styles.track, !!inputExtra && styles.inputExtra)}>
          {props.type === 'select' ? (
            <InputSelect
              {...{
                variants,
                label,
                description,
                staticPrefix,
                inputExtra,
                error
              }}
              {...props}
              className={clsx(
                variants?.map((variant) => styles[variant]),
                props.className
              )}
              ref={ref}
            />
          ) : props.type === 'add-options' ? (
            <InputAddOptions
              {...{
                variants,
                label,
                description,
                staticPrefix,
                inputExtra,
                error
              }}
              {...props}
              className={clsx(
                variants?.map((variant) => styles[variant]),
                props.className
              )}
              ref={ref}
            />
          ) : (
            <div
              className={clsx(
                styles.inputBar,
                !!staticPrefix && styles.staticPrefix,
                props.className
              )}
            >
              {staticPrefix && (
                <span className={styles.staticPrefix}>{staticPrefix}</span>
              )}
              <input
                {...props}
                className={clsx(variants?.map((variant) => styles[variant]))}
                ref={ref}
              />
            </div>
          )}{' '}
          {inputExtra && <div className={styles.extra}>{inputExtra}</div>}
        </div>
        {error && <div className={clsx(styles.box, styles.error)}>{error}</div>}
      </div>
    );
  }
);

Input.displayName = 'Input';
