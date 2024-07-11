import { PageFrame } from '../../../components';

import { CastVoteForm } from './form';
import styles from './page.module.scss';

export default async function CreatePollPage() {
  return (
    <PageFrame>
      <section className={styles.main}>
        <CastVoteForm />
      </section>
    </PageFrame>
  );
}
