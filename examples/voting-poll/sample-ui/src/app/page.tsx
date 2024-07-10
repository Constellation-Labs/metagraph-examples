'use client';

import { useForm } from 'react-hook-form';

import { Input, PageFrame } from '../components';
import { buildRegisterField } from '../utils';

export default function HomePage() {
  const form = useForm();

  const register = buildRegisterField(
    form.register,
    form.formState,
    form.control
  );
  return (
    <PageFrame>
      <Input label="label" {...register('a')} />
      <Input label="label" description="decription" {...register('a')} />
      <Input
        label="label"
        description="decription"
        placeholder="placeholder"
        {...register('a')}
      />
      <Input
        type="select"
        label="label"
        options={[{ value: 'opt1' }, { value: 'opt2' }]}
        {...register('b')}
      />
      <Input
        type="select"
        description="decription"
        label="label"
        options={[{ value: 'opt1' }, { value: 'opt2' }]}
        {...register('b')}
      />
      <Input
        type="select"
        description="decription"
        label="label"
        placeholder="placeholder"
        options={[{ value: 'opt1' }, { value: 'opt2' }]}
        {...register('b')}
      />
      <Input type="add-options" label="label" {...register('c')} />
      <Input
        type="add-options"
        label="label"
        description="decription"
        {...register('c')}
      />
      <Input
        type="add-options"
        label="label"
        description="decription"
        placeholder="placeholder"
        {...register('c')}
      />
    </PageFrame>
  );
}
