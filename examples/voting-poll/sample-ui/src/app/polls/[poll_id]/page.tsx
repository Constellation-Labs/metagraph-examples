import { PageFrame } from '../../../components';

import styles from './page.module.scss';

export default async function PollPage() {
  return (
    <PageFrame>
      <section className={styles.main}></section>
    </PageFrame>
  );
}
