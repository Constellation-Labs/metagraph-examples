import { PageFrame, PollCard } from '../../components';
import { ButtonLink } from '../../components/Button/ButtonLink/component';
import { MetagraphBaseURLs } from '../../consts';
import { IPoll } from '../../types';

import styles from './page.module.scss';

export default async function HomePage() {
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

  const polls: [string, IPoll][] = await response.json();

  return (
    <PageFrame>
      {polls.length === 0 && (
        <section className={styles.noPolls}>
          <span>No polls yet, create one!</span>
          <ButtonLink variants={['primary']} href={'/polls/create'}>
            Create Poll
          </ButtonLink>
        </section>
      )}
      <section className={styles.main}>
        {polls.map(([id, poll]) => (
          <PollCard key={id} poll={poll} />
        ))}
      </section>
    </PageFrame>
  );
}
