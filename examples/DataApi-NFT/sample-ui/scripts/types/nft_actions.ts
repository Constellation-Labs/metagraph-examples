type MintCollectionAction = {
  MintCollection: { name: string };
};

type MintCollectionNftAction = {
  MintNFT: {
    owner: string;
    collectionId: string;
    nftId: number;
    uri: string;
    name: string;
    description: string;
    metadata: Record<string, string>;
  };
};

type TransferCollectionAction = {
  TransferCollection: {
    fromAddress: string;
    toAddress: string;
    collectionId: string;
  };
};

type TransferCollectionNftAction = {
  TransferNFT: {
    fromAddress: string;
    toAddress: string;
    collectionId: string;
    nftId: number;
  };
};

type MetagraphNftCollectionAction =
  | MintCollectionAction
  | MintCollectionNftAction
  | TransferCollectionAction
  | TransferCollectionNftAction;

export type {
  MintCollectionAction,
  MintCollectionNftAction,
  TransferCollectionAction,
  TransferCollectionNftAction,
  MetagraphNftCollectionAction
};
