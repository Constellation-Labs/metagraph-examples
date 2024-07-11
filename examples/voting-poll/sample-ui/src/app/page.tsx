'use client';

import { dag4 } from '@stardust-collective/dag4';

import { PageFrame, PollCard } from '../components';

import styles from './page.module.scss';

export default function HomePage() {
  return (
    <PageFrame>
      <section className={styles.main}>
        <PollCard
          poll={{
            name: '$poll_name$',
            owner: dag4.keyStore.getDagAddressFromPrivateKey(
              dag4.keyStore.generatePrivateKey()
            ),
            pollOptions: ['Opt1', 'Opt2', 'Opt3', 'Opt4'],
            startSnapshotOrdinal: 1129321,
            endSnapshotOrdinal: 1229321,
            results: [
              { option: 'Opt1', votes: 1281 },
              { option: 'Opt2', votes: 4352 },
              { option: 'Opt3', votes: 1733 },
              { option: 'Opt4', votes: 2957 }
            ]
          }}
        />
        <PollCard
          poll={{
            name: '$poll_name$',
            owner: dag4.keyStore.getDagAddressFromPrivateKey(
              dag4.keyStore.generatePrivateKey()
            ),
            pollOptions: ['Opt1', 'Opt2', 'Opt3', 'Opt4'],
            startSnapshotOrdinal: 1129321,
            endSnapshotOrdinal: 1229321
          }}
        />
        <PollCard
          poll={{
            name: '$poll_name$',
            owner: dag4.keyStore.getDagAddressFromPrivateKey(
              dag4.keyStore.generatePrivateKey()
            ),
            pollOptions: ['Opt1', 'Opt2', 'Opt3', 'Opt4'],
            startSnapshotOrdinal: 1129321,
            endSnapshotOrdinal: 1229321
          }}
        />
        <PollCard
          poll={{
            name: '$poll_name$',
            owner: dag4.keyStore.getDagAddressFromPrivateKey(
              dag4.keyStore.generatePrivateKey()
            ),
            pollOptions: ['Opt1', 'Opt2', 'Opt3', 'Opt4'],
            startSnapshotOrdinal: 1129321,
            endSnapshotOrdinal: 1229321
          }}
        />
        <PollCard
          poll={{
            name: '$poll_name$',
            owner: dag4.keyStore.getDagAddressFromPrivateKey(
              dag4.keyStore.generatePrivateKey()
            ),
            pollOptions: ['Opt1', 'Opt2', 'Opt3', 'Opt4'],
            startSnapshotOrdinal: 1129321,
            endSnapshotOrdinal: 1229321
          }}
        />
      </section>
    </PageFrame>
  );
}
