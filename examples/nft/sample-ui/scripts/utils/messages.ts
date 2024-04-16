import { MetagraphNftCollectionAction } from 'scripts/types/nft_actions.ts';
import axios, { AxiosError } from 'axios';

import { dag4 } from '@stardust-collective/dag4';

import { EnvironmentContext } from '../../src/lib/index.ts';

const isAxiosError = (value: any): value is AxiosError => {
  return value?.isAxiosError === true;
};

const generateActionMessageProof = async (
  actionMessage: MetagraphNftCollectionAction,
  signingAccount: typeof dag4.account
) => {
  const encodedMessage = Buffer.from(JSON.stringify(actionMessage)).toString('base64')
  const signature = await dag4.keyStore.dataSign(
    signingAccount.keyTrio.privateKey,
    encodedMessage
  );

  const publicKey = signingAccount.keyTrio.publicKey;
  const uncompressedPublicKey =
    publicKey.length === 128 ? '04' + publicKey : publicKey;

  return {
    id: uncompressedPublicKey.substring(2),
    signature
  };
};

const generateActionMessageBody = async (
  actionMessage: MetagraphNftCollectionAction,
  signingAccount: typeof dag4.account
) => {
  const proof = await generateActionMessageProof(actionMessage, signingAccount);

  const body = { value: actionMessage, proofs: [proof] };

  return body;
};

const sendActionMessage = async (
  actionMessage: MetagraphNftCollectionAction,
  signingAccount: typeof dag4.account
) => {
  const body = await generateActionMessageBody(actionMessage, signingAccount);

  let response;
  try {
    console.log('Sending Action Message:');
    console.log(JSON.stringify(body, null, 2));

    response = await axios.post(
      EnvironmentContext.metagraphL1DataUrl + '/data',
      body
    );

    console.log('Response Data');
    console.log(JSON.stringify(response.data, null, 2));
  } catch (e) {
    if (isAxiosError(e)) {
      console.log(`Status: ${e.status}`);
      console.log(JSON.stringify(e.response?.data, null, 2));
      throw new Error('Send Action Message Error: See above for details');
    }
    throw e;
  }

  return response;
};

export {
  generateActionMessageProof,
  generateActionMessageBody,
  sendActionMessage
};
