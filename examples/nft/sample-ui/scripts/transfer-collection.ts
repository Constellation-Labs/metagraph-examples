import { Command } from 'commander';
import boxen from 'boxen';

import 'dotenv/config';
import { dag4 } from '@stardust-collective/dag4';

import { sendActionMessage } from './utils/messages.ts';

const transferCollection = async (options: {
  accountPk: string;
  collectionId: string;
  fromAddress: string;
  toAddress: string;
}): Promise<void> => {
  console.log(
    boxen('Transfering Collection', {
      padding: 1,
      borderStyle: 'double'
    })
  );

  const account = dag4.createAccount(options.accountPk);

  console.log(`Account Details`);
  console.dir(account.keyTrio, {});

  await sendActionMessage(
    {
      TransferCollection: {
        fromAddress: options.fromAddress,
        toAddress: options.toAddress,
        collectionId: options.collectionId
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
program.requiredOption('-f, --from-address <from-address>', 'From address');
program.requiredOption('-t, --to-address <to-address>', 'To address');
program.action(transferCollection);

program.parseAsync();
