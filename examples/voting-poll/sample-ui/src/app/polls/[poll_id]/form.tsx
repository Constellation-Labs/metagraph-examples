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
  signedPayload: z
    .string({ invalid_type_error: 'Invalid signed payload' })
    .regex(
      /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/,
      'Invalid base64 character'
    )
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

  const buildAndSignPayload = async () => {
    const values = getValues();

    const basePayload = {
      VoteInPoll: {
        pollId: values.pollId,
        address: values.address,
        option: values.option
      }
    };

    const { signature, pub } = await requestDataSignature(basePayload);

    const uncompressedPublicKey = pub.length === 128 ? '04' + pub : pub;

    const fullPayload = {
      value: {
        ...basePayload
      },
      proofs: [{ id: uncompressedPublicKey.substring(2), signature }]
    };

    return btoa(JSON.stringify(fullPayload));
  };

  useEffect(() => {
    setValue('address', wallet.active ? wallet.account : '', {
      shouldTouch: true,
      shouldDirty: true
    });
  }, [wallet.active && wallet.account]);

  useEffect(() => {
    const subscription = watch((_, { name }) => {
      if (name !== 'signedPayload') {
        setValue('signedPayload', '', {
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
            {...registerField('signedPayload')}
          />
          <Button
            type="button"
            variants={['primary']}
            onClick={async () => {
              setValue('signedPayload', await buildAndSignPayload(), {
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
