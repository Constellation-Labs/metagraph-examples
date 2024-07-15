'use client';

import { Id, toast } from 'react-toastify';

export const useToastAction = <
  Executor extends (...args: any[]) => Promise<any>
>(
  executor: Executor,
  options: {
    progress?: (toastFn: typeof toast) => Id;
    success?: (toastFn: typeof toast) => Id;
    error?: (toastFn: typeof toast, error: unknown) => Id;
  }
) => {
  const wrapperExecutor = (async (...args: Parameters<Executor>) => {
    const progressId = options.progress && options.progress(toast);
    try {
      const response = await executor(...args);
      progressId !== undefined && toast.dismiss(progressId);
      options.success && options.success(toast);
      return response;
    } catch (e) {
      progressId !== undefined && toast.dismiss(progressId);
      options.error && options.error(toast, e);
      throw e;
    }
  }) as Executor;

  return wrapperExecutor;
};
