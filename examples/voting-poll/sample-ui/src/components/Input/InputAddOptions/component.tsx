'use client';

import clsx from 'clsx';
import React from 'react';
import { useController } from 'react-hook-form';

import { IInputProps, Input } from '../component';
import { Button } from '../../Button/component';

import styles from './component.module.scss';

const parseArray = (value: any): string[] => {
  if (typeof value === 'string') {
    try {
      return parseArray(JSON.parse(value));
    } catch (e) {
      void 0;
    }
  }

  if (typeof value === 'string') {
    return value
      .split(',')
      .map((value) => value.trim())
      .filter((value) => value !== '');
  }

  if (typeof value === 'object' && Array.isArray(value)) {
    return value.map((option) => String(option));
  }

  return [];
};

export const InputAddOptions = React.forwardRef<HTMLInputElement, IInputProps>(
  (
    { variants, label, description, inputExtra, error, control, ...props },
    ref
  ) => {
    const { field } = useController({ control, name: props.name ?? '' });

    const values = parseArray(field.value);

    if (values.length === 0) {
      values.push('');
    }

    return (
      <div
        className={clsx(
          styles.main,
          error && styles.error,
          variants?.map((variant) => styles[variant]),
          props.className
        )}
      >
        <div className={styles.options}>
          {values.map((value, idx) => (
            <Input
              key={idx}
              variants={variants}
              onBlur={field.onBlur}
              value={value}
              onChange={(event) => {
                const newValues = [...values];
                newValues[idx] = event.target.value;
                field.onChange(newValues);
              }}
              onKeyDown={(event) => {
                if (
                  event.key === 'Backspace' &&
                  event.currentTarget.value === ''
                ) {
                  const newValues = [...values];
                  newValues.splice(idx, 1);
                  field.onChange(newValues);
                  event.preventDefault();
                }
              }}
            />
          ))}
        </div>
        <div className={styles.controls}>
          <Button
            className={styles.add}
            variants={['primary', 'padding-sm', 'full-width']}
            onClick={() => {
              const newValues = [...values];
              newValues.push('');
              field.onChange(newValues);
            }}
          >
            + add option
          </Button>
        </div>
      </div>
    );
  }
);

InputAddOptions.displayName = 'InputSortableOptions';
