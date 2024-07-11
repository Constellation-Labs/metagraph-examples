'use server';

import { revalidatePath, revalidateTag } from 'next/cache';
import { redirect } from 'next/navigation';
import { z } from 'zod';

import { PollSchema } from '../../../schemas';

const CreatePollSchema = PollSchema.extend({
  signature: z
    .string({ invalid_type_error: 'Invalid signature' })
    .length(128, 'Must contain 128 hex character(s)')
    .regex(/^([a-fA-F0-9]{2,})$/, 'Invalid hex character')
});

type ICreatePollSchema = z.infer<typeof CreatePollSchema>;

export const createPoll = async (values: ICreatePollSchema) => {
  const validatedFields = CreatePollSchema.safeParse(values);

  console.log(values);

  if (!validatedFields.success) {
    return { errors: validatedFields.error.flatten().fieldErrors };
  }

  const pollHash = 'hash1';

  revalidateTag('polls');
  revalidatePath('/polls');
  redirect(`/polls/${pollHash}`);
};
