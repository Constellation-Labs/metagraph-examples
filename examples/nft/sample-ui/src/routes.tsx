import { Navigate, RouteObject } from 'react-router-dom';

import { DashboardView, CollectionView, NftView } from './views/index.ts';

const routes: RouteObject[] = [
  { path: '/', index: true, element: <Navigate to="dashboard" /> },
  {
    path: '/dashboard',
    element: <DashboardView />
  },
  {
    path: '/collections/:collectionId',
    element: <CollectionView />
  },
  {
    path: '/collections/:collectionId/nfts/:nftId',
    element: <NftView />
  }
];

export { routes };
