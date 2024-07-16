import { notFound } from 'next/navigation';

import { PageFrame, RequiredWallet } from '../../../components';
import { MetagraphBaseURLs } from '../../../consts';
import { IPoll } from '../../../types';

import { CastVoteForm } from './form';
import styles from './page.module.scss';

export default async function CastVotePage({
  params
}: {
  params: { poll_id: string };
}) {
  const response = await fetch(
    MetagraphBaseURLs.metagraphL0 + `/data-application/polls/${params.poll_id}`,
    { cache: 'no-store' }
  );

  if (response.status === 404) {
    notFound();
  }

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

  const poll: IPoll = await response.json();

  return (
    <PageFrame>
      <RequiredWallet>
        <section className={styles.main}>
          <CastVoteForm poll={poll} />
        </section>
      </RequiredWallet>
    </PageFrame>
  );
}
