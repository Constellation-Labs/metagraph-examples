'use client';

import { useEffect, useState } from 'react';

export const useAsyncAction = <
  Executor extends (...args: any[]) => Promise<any>
>(
  executor: Executor,
  deps?: any[]
) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown | false>(false);

  useEffect(() => {
    setLoading(false);
    setError(false);
  }, deps);

  const wrapperExecutor = (async (...args: Parameters<Executor>) => {
    setLoading(true);
    try {
      const response = await executor(...args);
      setLoading(false);
      return response;
    } catch (e) {
      setError(e);
      setLoading(false);
      throw e;
    }
  }) as Executor;

  return [wrapperExecutor, { loading, error }] as const;
};
