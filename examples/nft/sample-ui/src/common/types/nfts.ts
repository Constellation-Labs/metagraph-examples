type IMetagraphNft = {
  serial: string;
  name: string;
  collectionAddress: string;
  ownerAddress: string;
  illustrationUrl: string;
  traits: string[];
};

type IMetagraphNftCollection = {
  name: string;
  address: string;
  ownerAddress: string;
  headerUrl: string;
  length?: number;
  items: IMetagraphNft[];
};

type ICollectionNFTAPIResponse = {
  id: 0;
  collectionId: string;
  owner: string;
  uri: string;
  name: string;
  description: string;
  creationDateTimestamp: number;
  metadata: Record<string, string>;
};

type ICollectionAPIResponse = {
  id: string;
  owner: string;
  name: string;
  creationDateTimestamp: number;
  numberOfNFTs: number;
};

export {
  type IMetagraphNft,
  type IMetagraphNftCollection,
  type ICollectionAPIResponse,
  type ICollectionNFTAPIResponse
};
