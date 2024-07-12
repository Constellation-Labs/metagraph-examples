'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useEffect } from 'react';

import { ButtonLink } from '../../../components/Button/ButtonLink/component.tsx';
import { useWalletProvider } from '../../../providers/index.ts';
import { PollSchema } from '../../../schemas/index.ts';
import { buildRegisterField } from '../../../utils/forms.ts';
import { Button, Card, Input } from '../../../components/index.ts';

import styles from './page.module.scss';
import { createPoll } from './actions.ts';

const FormPollSchema = PollSchema.extend({
  signedPayload: z
    .string({ invalid_type_error: 'Invalid signed payload' })
    .regex(
      /^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/,
      'Invalid base64 character'
    )
});

type IFormPollSchema = z.infer<typeof FormPollSchema>;

export const CreatePollForm = () => {
  const { wallet, requestDataSignature } = useWalletProvider();

  const {
    formState,
    register,
    handleSubmit,
    control,
    setValue,
    getValues,
    watch
  } = useForm<IFormPollSchema>({
    mode: 'onTouched',
    resolver: zodResolver(FormPollSchema)
  });

  const registerField = buildRegisterField(register, formState, control);

  const onFormSubmit = handleSubmit(async (values) => {
    await createPoll(values);
  });

  const buildAndSignPayload = async () => {
    const values = getValues();

    const basePayload = {
      CreatePoll: {
        name: values.name,
        owner: values.owner,
        pollOptions: values.options,
        startSnapshotOrdinal: values.startSnapshotOrdinal,
        endSnapshotOrdinal: values.endSnapshotOrdinal
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
    setValue('owner', wallet.active ? wallet.account : '', {
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
        <Card variants={['full-width', 'padding-m']} header={'Poll'}>
          <Input
            label="Name"
            description="Maximum of 128 characters"
            placeholder="Enter a poll name"
            {...registerField('name')}
          />
          <Input
            label="Owner"
            description="Owner address"
            readOnly
            {...registerField('owner')}
          />
          <Input
            type="add-options"
            label="Options"
            description="Poll options"
            {...registerField('options')}
          />
          <Input
            label="Start snaphot ordinal"
            description="When the poll should start"
            placeholder="Enter an ordinal number"
            {...registerField('startSnapshotOrdinal')}
          />
          <Input
            label="End snaphot ordinal"
            description="When the poll should end"
            placeholder="Enter an ordinal number"
            {...registerField('endSnapshotOrdinal')}
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
          Create poll
        </Button>
      </Card>
    </form>
  );
};
