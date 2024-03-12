import { Command } from 'commander';
import boxen from 'boxen';

import 'dotenv/config';
import { dag4 } from '@stardust-collective/dag4';

import { sendActionMessage } from './utils/messages.ts';

const mintCollectionNft = async (options: {
  accountPk: string;
  collectionId: string;
  nftId: string;
  nftName: string;
  nftDescription: string;
  nftUri: string;
  nftOwner: string;
}): Promise<void> => {
  console.log(
    boxen('Minting Collection NFT', {
      padding: 1,
      borderStyle: 'double'
    })
  );

  const account = dag4.createAccount(options.accountPk);

  console.log(`Account Details`);
  console.dir(account.keyTrio, {});

  await sendActionMessage(
    {
      MintNFT: {
        owner: options.nftOwner,
        collectionId: options.collectionId,
        nftId: parseInt(options.nftId),
        uri: options.nftUri,
        name: options.nftName,
        description: options.nftDescription,
        metadata: {}
      }
    },
    account
  );
};

const program = new Command();
program.requiredOption(
  '-k, --account-pk <account-pk>',
  'Account private key to use'
);
program.requiredOption('-c, --collection-id <collection-id>', 'Collection Id');
program.requiredOption('-i, --nft-id <nft-id>', 'NFT id');
program.requiredOption('-n, --nft-name <nft-name>', 'NFT name');
program.requiredOption(
  '-d, --nft-description <nft-description>',
  'NFT description'
);
program.requiredOption('-u, --nft-uri <nft-uri>', 'NFT uri');
program.requiredOption('-o, --nft-owner <nft-owner>', 'NFT owner');
program.action(mintCollectionNft);

program.parseAsync();
