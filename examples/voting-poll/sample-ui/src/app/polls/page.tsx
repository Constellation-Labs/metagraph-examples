'use client';

import { useEffect, useState } from 'react';

/* eslint-disable react/no-unescaped-entities */
import { Card, PageFrame, PollCard } from '../../components';
import { ButtonLink } from '../../components/Button/ButtonLink/component';
import { IPoll } from '../../types';

import styles from './page.module.scss';
import { getPolls } from './actions';

export default function HomePage() {
  const [polls, setPolls] = useState<[string, IPoll][]>([]);

  const fetchPolls = async () => {
    setPolls(await getPolls());
  };

  useEffect(() => {
    const iid = window.setInterval(fetchPolls, 5 * 1000);
    fetchPolls();
    return () => {
      window.clearInterval(iid);
    };
  }, []);

  return (
    <PageFrame variants={['noSidebarMargin']}>
      <section className={styles.introCard}>
        <Card
          header="Welcome to Metagraph Voting Poll Example"
          variants={['padding-m']}
        >
          <p>
            This application demonstrates the usage of a metagraph as a web
            server backend and the integration of Stargazer wallet to securely
            sign and send custom data updates to the metagraph. Polls can be
            created with custom voting options, which can then be voted on using
            the token balance of your wallet. Note that tokens are required in
            your wallet to vote. Please see the included README.md file for
            instructions on how to add L0 tokens to your wallet.
            <br />
            <br />
            Metagraph state can be queried through the following links:
            <ul>
              <li>
                <a
                  href="http://localhost:9200/snapshots/latest/combined"
                  target="_blank"
                >
                  http://localhost:9200/snapshots/latest/combined
                </a>
                : View the latest snapshot data.{' '}
              </li>
              <li>
                <a
                  href="http://localhost:9200/data-application/polls"
                  target="_blank"
                >
                  http://localhost:9200/data-application/polls
                </a>
                : View a custom GET endpoint, exposing all polls through
                metagraph Calculated State.
              </li>
            </ul>
          </p>
          <div className={styles.createPolls}>
            {polls.length === 0 && <span>No polls yet, create one!</span>}
            <ButtonLink variants={['primary']} href={'/polls/create'}>
              Create Poll
            </ButtonLink>
          </div>
        </Card>
      </section>
      <section className={styles.main}>
        {polls.map(([id, poll]) => (
          <PollCard key={id} poll={poll} />
        ))}
      </section>
    </PageFrame>
  );
}
