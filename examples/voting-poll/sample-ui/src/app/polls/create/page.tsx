import { PageFrame, RequiredWallet } from '../../../components';

import { CreatePollForm } from './form';
import styles from './page.module.scss';

export default async function CreatePollPage() {
  return (
    <PageFrame>
      <RequiredWallet>
        <section className={styles.main}>
          <CreatePollForm />
        </section>
      </RequiredWallet>
    </PageFrame>
  );
}
