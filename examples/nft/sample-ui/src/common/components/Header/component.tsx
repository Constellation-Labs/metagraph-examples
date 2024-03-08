import { Title } from '@mantine/core';

import ConstellationLogoSvg from '../../../assets/logos/constellation_logo.svg';

import styles from './component.module.scss';

const Header = () => {
  return (
    <div className={styles.main}>
      <div className={styles.content}>
        <img src={ConstellationLogoSvg} alt="Neo Logo" />
        <div className={styles.titles}>
          <Title order={1}>Metagraph - NFT Example</Title>
        </div>
      </div>
    </div>
  );
};

export { Header };
