import { getEnvOrError } from '../../utils/index.ts';

const makeEnvironmentContext = <E extends Record<string, () => any>>(
  envDefinition: E
) => {
  return new Proxy(
    {},
    {
      get: (t, p) => {
        if (typeof p !== 'string') {
          throw new Error('Inconsistency error');
        }

        return envDefinition[p]();
      }
    }
  ) as { [K in keyof E]: ReturnType<E[K]> };
};

const expandByPrefixes = (prefixes: string[]) => (term: string) =>
  prefixes.map((prefix) => `${prefix}${term}`);

const expandByCommonClientPrefixes = expandByPrefixes([
  'REACT_APP_',
  'NEXT_PUBLIC_'
]);

const EnvironmentContext = makeEnvironmentContext({
  nodeEnv: () => getEnvOrError<string>('NODE_ENV'),
  baseApiUrl: () =>
    getEnvOrError<string>(
      'METAGRAPH_L0_URL',
      ...expandByCommonClientPrefixes('METAGRAPH_L0_URL')
    ),
  globalL0Url: () => getEnvOrError<string>('GLOBAL_L0_URL'),
  metagraphL1DataUrl: () => getEnvOrError<string>('METAGRAPH_L1_DATA_URL')
});

export { EnvironmentContext };
