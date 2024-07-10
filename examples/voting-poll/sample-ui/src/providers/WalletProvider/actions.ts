'use server';

import { cookies } from 'next/headers';

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
