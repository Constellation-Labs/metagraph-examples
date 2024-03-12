import { createProviderStateHook } from '@/utils/index.ts';

import { ApiProvider, ApiProviderContext } from './provider.tsx';

const useApiProvider = createProviderStateHook(ApiProvider, ApiProviderContext);

export { useApiProvider };
