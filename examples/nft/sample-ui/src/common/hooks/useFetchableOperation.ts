import { useState } from 'react';

import { FetchStatus, isFetchStatus } from '../consts/index.ts';

type FetchableOperation = {
  status: FetchStatus;
  error: Error | null;
  setStatus: (status: FetchStatus) => void;
  fetch: (valueWrapped: Promise<any | FetchStatus>) => Promise<void>;
  wrappedFetch: <T extends (...args: any[]) => Promise<any | FetchStatus>>(
    fetcher: T
  ) => T;
};

const useFetchableOperation = () => {
  const [status, setStatus] = useState<FetchStatus>(FetchStatus.IDLE);
  const [error, setError] = useState<Error | null>(null);

  const operation: FetchableOperation = {
    status,
    error,
    setStatus: (status) => setStatus(status),
    fetch: async (valueWrapped) => {
      try {
        setError(null);
        setStatus(FetchStatus.PENDING);
        const value = await valueWrapped;
        if (isFetchStatus(value)) {
          setStatus(value);
        } else {
          setStatus(FetchStatus.DONE);
        }
      } catch (e) {
        console.error(e);
        setStatus(FetchStatus.ERROR);
        setError(e instanceof Error ? e : new Error('Unknown error'));
        throw e;
      }
    },
    wrappedFetch: (fetcher) => {
      return ((...args: Parameters<typeof fetcher>) => {
        const result = fetcher(...args);
        operation.fetch(result);
        return result;
      }) as typeof fetcher;
    }
  };

  return operation;
};

const useFetchableOperations = <Keys extends string>(keys: Keys[]) => {
  const uniqueKeys = [...new Set(keys)].sort();

  const [statuses, setStatuses] = useState(() => {
    const statuses = {} as Record<Keys, FetchStatus>;
    for (const key of uniqueKeys) {
      statuses[key] = FetchStatus.IDLE;
    }
    return statuses;
  });
  const [errors, setErrors] = useState(() => {
    const errors = {} as Record<Keys, Error | null>;
    for (const key of uniqueKeys) {
      errors[key] = null;
    }
    return errors;
  });

  const operations = {} as Record<Keys, FetchableOperation>;

  for (const key of uniqueKeys) {
    const operation: FetchableOperation = {
      status: statuses[key],
      error: errors[key],
      setStatus: (status) => setStatuses((sts) => ({ ...sts, [key]: status })),
      fetch: async (valueWrapped) => {
        try {
          setErrors((sts) => ({ ...sts, [key]: null }));
          setStatuses((sts) => ({ ...sts, [key]: FetchStatus.PENDING }));
          const value = await valueWrapped;
          if (isFetchStatus(value)) {
            setStatuses((sts) => ({ ...sts, [key]: value }));
          } else {
            setStatuses((sts) => ({ ...sts, [key]: FetchStatus.DONE }));
          }
        } catch (e) {
          console.error(e);
          setStatuses((sts) => ({ ...sts, [key]: FetchStatus.ERROR }));
          setErrors((sts) => ({
            ...sts,
            [key]: e instanceof Error ? e : new Error('Unknown error')
          }));
          throw e;
        }
      },
      wrappedFetch: (fetcher) => {
        return ((...args: Parameters<typeof fetcher>) => {
          const result = fetcher(...args);
          operation.fetch(result);
          return result;
        }) as typeof fetcher;
      }
    };
    operations[key] = operation;
  }

  return operations;
};

export {
  type FetchableOperation,
  useFetchableOperation,
  useFetchableOperations
};
