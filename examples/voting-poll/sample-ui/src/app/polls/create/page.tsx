import { PageFrame } from '../../../components';

import styles from './page.module.scss';

export default async function CreatePollPage() {
  return (
    <PageFrame>
      <section className={styles.main}></section>
    </PageFrame>
  );
}
