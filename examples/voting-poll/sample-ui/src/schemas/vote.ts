import { z } from 'zod';

export const VoteSchema = z.object({
  pollId: z
    .string({ invalid_type_error: 'Invalid poll id' })
    .min(1, 'Must contain at least 1 character(s)')
    .max(128, { message: 'Must contain at most 128 character(s)' }),
  address: z
    .string({ invalid_type_error: 'Invalid voter address' })
    .min(1, 'Must contain at least 1 character(s)')
    .max(128, { message: 'Must contain at most 128 character(s)' }),
  option: z
    .string({ invalid_type_error: 'Invalid vote option' })
    .min(1, 'Must contain at least 1 character(s)')
    .max(128, { message: 'Must contain at most 128 character(s)' })
});
