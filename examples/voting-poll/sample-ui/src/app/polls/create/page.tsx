import { PageFrame } from '../../../components';

import { CreatePollForm } from './form';
import styles from './page.module.scss';

export default async function CreatePollPage() {
  return (
    <PageFrame>
      <section className={styles.main}>
        <CreatePollForm />
      </section>
    </PageFrame>
  );
}
