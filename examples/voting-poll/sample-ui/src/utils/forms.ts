import { Control, FormState, UseFormRegister } from 'react-hook-form';

export const buildRegisterField = <
  F extends UseFormRegister<any>,
  S extends FormState<any>,
  C extends Control<any>
>(
  fn: F,
  formState: S,
  control: C
): F => {
  return ((name, ...args) => {
    const result = fn(name, ...args);
    const error = formState.errors[name]?.message;

    return Object.assign(result, { error, control }) as any;
  }) as F;
};
