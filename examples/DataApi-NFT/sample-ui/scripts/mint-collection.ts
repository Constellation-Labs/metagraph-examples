import { Command } from 'commander';
import boxen from 'boxen';

import 'dotenv/config';
import { dag4 } from '@stardust-collective/dag4';

import { sendActionMessage } from './utils/messages.ts';

const mintCollection = async (options: {
  accountPk: string;
  collectionName: string;
}): Promise<void> => {
  console.log(
    boxen('Minting Collection', {
      padding: 1,
      borderStyle: 'double'
    })
  );

  const account = dag4.createAccount(options.accountPk);

  console.log(`Account Details`);
  console.dir(account.keyTrio, {});

  await sendActionMessage(
    { MintCollection: { name: options.collectionName } },
    account
  );
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
program.action(mintCollection);

program.parseAsync();
