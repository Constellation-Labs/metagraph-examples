'use server';

import { MetagraphBaseURLs } from '../../consts';
import { IMetagraphL0Snapshot } from '../../types';

export const getMetagraphLatestSnapshot = async () => {
  const response = await fetch(
    MetagraphBaseURLs.metagraphL0 + '/snapshots/latest',
    {
      cache: 'no-store'
    }
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

  return (await response.json()) as IMetagraphL0Snapshot;
};
