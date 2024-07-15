/* eslint-disable react/no-unescaped-entities */
import { Card, PageFrame, PollCard } from '../../components';
import { ButtonLink } from '../../components/Button/ButtonLink/component';
import { MetagraphBaseURLs } from '../../consts';
import { IPoll } from '../../types';

import styles from './page.module.scss';

export default async function HomePage() {
  const polls: [string, IPoll][] = [];

  try {
    const response = await fetch(
      MetagraphBaseURLs.metagraphL0 + '/data-application/polls',
      { cache: 'no-store' }
    );

    if (response.status !== 200) {
      throw new Error(
        JSON.stringify(
          {
            errors: { serverErrors: [response.status, await response.text()] }
          },
          null,
          2
        )
      );
    }

    polls.push(...(await response.json()));
  } catch (e) {
    console.log(e);
  }

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
