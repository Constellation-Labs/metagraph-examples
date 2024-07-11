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
  const resultsTotal =
    poll.results?.reduce((pv, result) => pv + result.votes, 0) ?? 0;

  return (
    <Card
      className={styles.main}
      variants={['padding-m']}
      header={
        <span className={styles.header}>
          <span>Poll - {poll.name}</span>
          <span>Open</span>
        </span>
      }
    >
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
      <div className={styles.options}>
        {poll.pollOptions.map((option) => {
          const resultVotes =
            poll.results?.find((result) => result.option === option)?.votes ??
            0;
          const resultPercentage =
            resultsTotal === 0
              ? 0
              : Math.floor((resultVotes / resultsTotal) * 100 * 100) / 100;

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
                  {resultVotes !== 0 && ` (${resultVotes} votes)`}
                </span>
                {!!resultVotes && <span>{resultPercentage}%</span>}
              </div>
            </div>
          );
        })}
      </div>
      <ButtonLink variants={['primary']} href={`/polls/${poll.id}`}>
        Cast your vote
      </ButtonLink>
    </Card>
  );
};
