'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useEffect } from 'react';
import { useParams } from 'next/navigation';

import { ButtonLink } from '../../../components/Button/ButtonLink/component.tsx';
import { useWalletProvider } from '../../../providers/index.ts';
import { VoteSchema } from '../../../schemas/index.ts';
import { buildRegisterField } from '../../../utils/forms.ts';
import { Button, Card, Input } from '../../../components/index.ts';

import styles from './page.module.scss';
import { castVote } from './actions.ts';

const FormVoteSchema = VoteSchema.extend({
  signature: z
    .string({ invalid_type_error: 'Invalid signature' })
    .regex(/^([a-fA-F0-9]{2,})$/, 'Invalid hex character')
});

type IFormVoteSchema = z.infer<typeof FormVoteSchema>;

export const CastVoteForm = () => {
  const { poll_id } = useParams<{ poll_id: string }>();

  const { wallet, requestDataSignature } = useWalletProvider();

  const {
    formState,
    register,
    handleSubmit,
    control,
    setValue,
    getValues,
    watch
  } = useForm<IFormVoteSchema>({
    mode: 'onTouched',
    resolver: zodResolver(FormVoteSchema),
    defaultValues: { pollId: poll_id }
  });

  const registerField = buildRegisterField(register, formState, control);

  const onFormSubmit = handleSubmit(async (values) => {
    await castVote(values);
  });

  useEffect(() => {
    setValue('address', wallet.active ? wallet.account : '', {
      shouldTouch: true,
      shouldDirty: true
    });
  }, [wallet.active && wallet.account]);

  useEffect(() => {
    const subscription = watch((_, { name }) => {
      if (name !== 'signature') {
        setValue('signature', '', {
          shouldTouch: true,
          shouldDirty: true
        });
      }
    });
    return () => {
      subscription.unsubscribe();
    };
  }, []);

  return (
    <form className={styles.content} onSubmit={onFormSubmit}>
      <div className={styles.cards}>
        <Card variants={['full-width', 'padding-m']} header={'Cast vote'}>
          <Input
            label="Poll id"
            description="Your poll id"
            placeholder="Enter a poll id"
            readOnly
            {...registerField('pollId')}
          />
          <Input
            label="Address"
            description="Voter address"
            readOnly
            {...registerField('address')}
          />
          <Input
            label="Option"
            description="Vote option"
            {...registerField('option')}
          />
          <Input
            label="Signature"
            description="Your account signature for this poll information"
            readOnly
            {...registerField('signature')}
          />
          <Button
            type="button"
            variants={['primary']}
            onClick={async () => {
              const values = getValues();
              const { signature } = await requestDataSignature({
                VoteInPoll: {
                  pollId: values.pollId,
                  address: values.address,
                  option: values.option
                }
              });
              setValue('signature', signature, {
                shouldTouch: true,
                shouldDirty: true,
                shouldValidate: true
              });
            }}
          >
            Sign with your wallet
          </Button>
        </Card>
      </div>
      <Card variants={['padding-sm']}>
        <ButtonLink
          href={'/polls'}
          variants={['primary', 'outline-fade', 'centered']}
        >
          Cancel
        </ButtonLink>
        <Button
          variants={['primary', 'centered', !formState.isValid && 'disabled']}
          type="submit"
          disabled={!formState.isValid}
        >
          Cast vote
        </Button>
      </Card>
    </form>
  );
};
