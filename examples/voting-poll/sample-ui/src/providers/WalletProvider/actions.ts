'use server';

import { cookies } from 'next/headers';

import { MetagraphBaseURLs } from '../../consts';

import { WALLET_COOKIE_NAME } from './consts';

export const loginWallet = async (address: string) => {
  cookies().set(WALLET_COOKIE_NAME, address, { path: '/' });
};

export const logoutWallet = async () => {
  cookies().delete(WALLET_COOKIE_NAME);
};

export const getAuthenticatedWallet = async () => {
  return cookies().get(WALLET_COOKIE_NAME)?.value;
};

export const requestAddressBalance = async (address: string) => {
  const response = await fetch(
    MetagraphBaseURLs.metagraphL0 + `/currency/${address}/balance`
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

  const responseData: { balance: number } = await response.json();

  return responseData.balance / 1e8;
};
