'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useEffect } from 'react';
import { toast } from 'react-toastify';

import { ButtonLink } from '../../../components/Button/ButtonLink/component.tsx';
import { useWalletProvider } from '../../../providers/index.ts';
import { VoteSchema } from '../../../schemas/index.ts';
import { buildRegisterField } from '../../../utils/forms.ts';
import { Button, Card, Input } from '../../../components/index.ts';
import { IPoll } from '../../../types/poll.ts';
import { useToastAction } from '../../../utils/use_toast_action.ts';

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

type ICastVoteFormProps = { poll: IPoll };

export const CastVoteForm = ({ poll }: ICastVoteFormProps) => {
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
    defaultValues: { pollId: poll.id }
  });

  const registerField = buildRegisterField(register, formState, control);

  const onFormSubmit = handleSubmit(async (values) => {
    const response = await useToastAction(castVote, {
      progress: (t) => t.info('Submitting vote'),
      error: (t, e) => t.error(`Unknown error while submitting vote: ${e}`)
    })(values);

    if (response?.errors.serverErrors) {
      if (
        response.errors.serverErrors.content.includes('NotEnoughWalletBalance')
      ) {
        toast.error(
          'Unable to cast the vote, you need to hold balance in your wallet, check the project README.md for more info',
          { autoClose: false }
        );
        return;
      }
      toast.error(response.errors.serverErrors.content, { autoClose: false });
    }
  });

  const buildAndSignPayload = async () => {
    const validatedValues = FormVoteSchema.safeParse(getValues());

    if (!validatedValues.data) {
      throw new Error('Inconsistency Error');
    }

    const values = validatedValues.data;

    console.log(values);
    const basePayload = {
      VoteInPoll: {
        pollId: values.pollId,
        address: values.address,
        option: values.option
      }
    };

    const { signature, pub } = await useToastAction(requestDataSignature, {
      progress: (t) =>
        t.info('Requesting wallet signature', {
          autoClose: false
        }),
      success: (t) => t.success('Payload signed'),
      error: (t, e) =>
        t.error(`An error ocurred while signing the payload [${e}]`)
    })(basePayload);

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
            type="select"
            label="Option"
            description="Vote option"
            options={Object.keys(poll.result).map((option) => ({
              value: option
            }))}
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
