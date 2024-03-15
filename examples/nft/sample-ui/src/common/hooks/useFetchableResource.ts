import { useState } from 'react';

import {
  FetchStatus,
  FetchStatusValue,
  isFetchStatus,
  isFetchStatusValue
} from '../consts/index.ts';

type FetchableResource<R> = {
  resource: R;
  status: FetchStatus;
  fetch: (
    valueWrapped:
      | Promise<R | FetchStatusValue<R> | FetchStatus>
      | R
      | FetchStatusValue<R>
      | FetchStatus
  ) => Promise<void>;
  wrappedFetch: <
    T extends (...args: any[]) => Promise<R | FetchStatusValue<R> | FetchStatus>
  >(
    fetcher: T
  ) => T;
};

const useFetchableResource = <R>(initialState: R) => {
  const [resource, setResource] = useState<R>(initialState);
  const [status, setStatus] = useState<FetchStatus>(FetchStatus.IDLE);

  const fetchableResource: FetchableResource<R> = {
    resource,
    status,
    fetch: async (valueWrapped) => {
      try {
        setStatus(FetchStatus.PENDING);
        const value = await valueWrapped;
        if (isFetchStatusValue(value)) {
          setStatus(value.status);
          value.value && setResource(value.value);
        } else if (isFetchStatus(value)) {
          setStatus(value);
        } else {
          setResource(value);
          setStatus(FetchStatus.DONE);
        }
      } catch (e) {
        console.error(e);
        setStatus(FetchStatus.ERROR);
        throw e;
      }
    },
    wrappedFetch: (fetcher) => {
      return ((...args: Parameters<typeof fetcher>) => {
        const result = fetcher(...args);
        fetchableResource.fetch(result);
        return result;
      }) as typeof fetcher;
    }
  };

  return fetchableResource;
};

export { type FetchableResource, useFetchableResource };
