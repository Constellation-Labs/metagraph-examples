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
            This application leverages Constellation's Hypergraph Transfer
            Protocol (HGTP) to facilitate the creation and management of
            decentralized polls and votes. By connecting to a custom metagraph
            and interacting with the Stargazer Wallet, the app securely signs
            and submits payloads to the network.
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
                : Access the latest snapshot data.{' '}
              </li>
              <li>
                <a
                  href="http://localhost:9200/data-application/polls"
                  target="_blank"
                >
                  http://localhost:9200/data-application/polls
                </a>
                : Monitor the current state of polls.
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
