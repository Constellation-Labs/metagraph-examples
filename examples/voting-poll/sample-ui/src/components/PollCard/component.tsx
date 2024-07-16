'use client';

import Blockies from 'react-blockies';

import { IPoll } from '../../types';
import { shorten } from '../../utils';
import { Card } from '../Card/component';
import { ButtonLink } from '../Button/ButtonLink/component';

import styles from './component.module.scss';

export type IPollCardProps = {
  poll: IPoll;
};

export const PollCard = ({ poll }: IPollCardProps) => {
  const resultsTotal = Object.values(poll.result).reduce(
    (pv, votes) => pv + votes,
    0
  );

  return (
    <Card
      className={{ body: styles.main, root: styles.root }}
      variants={['padding-m']}
      header={
        <span className={styles.header}>
          <span>Poll - {poll.name}</span>
          <span>{poll.status}</span>
        </span>
      }
    >
      <div className={styles.info}>
        <span className={styles.creator}>
          Creator:
          <Blockies
            seed={poll.owner}
            size={10}
            scale={2}
            className={styles.identicon}
          />{' '}
          - {shorten(poll.owner)}
        </span>
        <span>
          Start: {poll.startSnapshotOrdinal} / End: {poll.endSnapshotOrdinal}
        </span>
      </div>

      <div className={styles.options}>
        {Object.entries(poll.result).map(([option, votes]) => {
          const resultPercentage =
            resultsTotal === 0
              ? 0
              : Math.floor((votes / resultsTotal) * 100 * 100) / 100;
          return (
            <div className={styles.option} key={option}>
              <span className={styles.placeholder}>placeholder</span>
              <div
                className={styles.bar}
                style={{ width: `${resultPercentage}%` }}
              ></div>
              <div className={styles.content}>
                <span>
                  {option}
                  {votes !== 0 && ` (${votes / 1e8} vp)`}
                </span>
                {!!votes && <span>{resultPercentage}%</span>}
              </div>
            </div>
          );
        })}
      </div>
      <ButtonLink
        className={styles.buttonLink}
        variants={['secondary']}
        href={`/polls/${poll.id}`}
      >
        Cast your vote
      </ButtonLink>
    </Card>
  );
};
