'use client';

import clsx from 'clsx';
import React from 'react';

import { IInputProps } from '../component';

import styles from './component.module.scss';

export const InputSelect = React.forwardRef<HTMLInputElement, IInputProps>(
  (
    {
      variants,
      label,
      description,
      inputExtra,
      error,
      control,
      options,
      ...props
    },
    ref
  ) => {
    return (
      <div
        className={clsx(
          styles.main,
          error && styles.error,
          variants?.map((variant) => styles[variant]),
          props.className
        )}
      >
        <select
          {...(props as any)}
          className={clsx(variants?.map((variant) => styles[variant]))}
          ref={ref as any}
        >
          {options &&
            options.map((option) => (
              <option
                className={styles.option}
                key={option.value}
                value={option.value}
              >
                {option.content ?? option.value}
              </option>
            ))}
        </select>
      </div>
    );
  }
);

InputSelect.displayName = 'InputSelect';
