'use server';

import { revalidatePath, revalidateTag } from 'next/cache';
import { redirect } from 'next/navigation';
import { z } from 'zod';

import { VoteSchema } from '../../../schemas';

const CastVoteSchema = VoteSchema.extend({
  signature: z
    .string({ invalid_type_error: 'Invalid signature' })
    .length(128, 'Must contain 128 hex character(s)')
    .regex(/^([a-fA-F0-9]{2,})$/, 'Invalid hex character')
});

type ICastVoteSchema = z.infer<typeof CastVoteSchema>;

export const castVote = async (values: ICastVoteSchema) => {
  const validatedFields = CastVoteSchema.safeParse(values);

  console.log(values);

  if (!validatedFields.success) {
    return { errors: validatedFields.error.flatten().fieldErrors };
  }

  const pollHash = 'hash1';

  revalidateTag('polls');
  revalidatePath('/polls');
  redirect(`/polls/${pollHash}`);
};
