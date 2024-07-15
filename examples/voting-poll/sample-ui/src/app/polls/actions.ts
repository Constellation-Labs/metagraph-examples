'use server';

import { MetagraphBaseURLs } from '../../consts';
import { IPoll } from '../../types';

export const getPolls = async () => {
  const polls: [string, IPoll][] = [];

  try {
    const response = await fetch(
      MetagraphBaseURLs.metagraphL0 + '/data-application/polls',
      { cache: 'no-store' }
    );

    if (response.status !== 200) {
      throw new Error(
        JSON.stringify(
          {
            errors: { serverErrors: [response.status, await response.text()] }
          },
          null,
          2
        )
      );
    }

    polls.push(...(await response.json()));
  } catch (e) {
    console.log(e);
  }

  return polls;
};
