'use server';

import { MetagraphBaseURLs } from '../../consts';
import { IMetagraphL0Snapshot } from '../../types';

export const getMetagraphLatestSnapshot = async () => {
  try {
    const response = await fetch(
      MetagraphBaseURLs.metagraphL0 + '/snapshots/latest',
      {
        cache: 'no-store'
      }
    );

    if (response.status !== 200) {
      return {
        errors: {
          serverErrors: {
            status: response.status,
            content: await response.text()
          }
        }
      };
    }

    return (await response.json()) as IMetagraphL0Snapshot;
  } catch (e) {
    return {
      errors: {
        serverErrors: {
          status: 500,
          content: String(e)
        }
      }
    };
  }
};
