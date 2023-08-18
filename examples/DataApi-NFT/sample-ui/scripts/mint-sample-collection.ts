import { Command } from 'commander';
import boxen from 'boxen';

import 'dotenv/config';
import { dag4 } from '@stardust-collective/dag4';

import { sendActionMessage } from './utils/messages.ts';

const KeyPool = new Array(50)
  .fill(null)
  .map(() => dag4.keyStore.generatePrivateKey());

const getRandomKeyFromPool = () =>
  KeyPool[Math.floor(Math.random() * KeyPool.length)];

const mintSampleCollection = async (options: {
  accountPk: string;
  collectionName: string;
  nftCount: string;
}): Promise<void> => {
  console.log(
    boxen('Minting Collection', {
      padding: 1,
      borderStyle: 'double'
    })
  );

  const account = dag4.createAccount(options.accountPk);

  console.log({ KeyPool });

  console.log(`Account Details`);
  console.dir(account.keyTrio, {});

  const response = await sendActionMessage(
    { MintCollection: { name: options.collectionName } },
    account
  );

  const collectionId = response.data.hash;

  if (typeof collectionId !== 'string') {
    throw new Error('Invalid collection id returned from response');
  }

  const nftCount = parseInt(options.nftCount);

  console.log('Waiting for collection to populate over the cluster');

  await new Promise((r) => setTimeout(r, 30 * 1000));

  console.log(`Minting ${nftCount} NFTs`);

  for (let i = 0; i < nftCount; i++) {
    const ownerAddress = dag4.keyStore.getDagAddressFromPrivateKey(
      getRandomKeyFromPool()
    );

    const serial = String(i).padStart(4, '0');
    const dtmSerial = String((i % 5000) + 1).padStart(4, '0');

    await sendActionMessage(
      {
        MintNFT: {
          owner: ownerAddress,
          collectionId,
          nftId: i,
          uri: `https://constellation-nfts-assets.s3.amazonaws.com/dtm/${dtmSerial}.png`,
          name: `${options.collectionName} - ${serial}`,
          description: `${options.collectionName} - ${serial} - desc`,
          metadata: {}
        }
      },
      account
    );
  }
};

const program = new Command();
program.requiredOption(
  '-k, --account-pk <account-pk>',
  'Account private key to use'
);
program.requiredOption(
  '-n, --collection-name <collection-name>',
  'Collection Name (must be unique)'
);
program.requiredOption('-c, --nft-count <nft-count>', 'NFT count to mint');
program.action(mintSampleCollection);

program.parseAsync();
