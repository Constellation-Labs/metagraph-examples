export type IMetagraphL0Proof = {
  id: string;
  signature: string;
};

export type IMetagraphL0Tip = {
  block: {
    height: number;
    hash: string;
  };
  usageCount: number;
  introducedAt: number;
};

export type IMetagraphL0Snapshot = {
  value: {
    ordinal: number;
    height: number;
    subHeight: number;
    lastSnapshotHash: string;
    blocks: [];
    rewards: [];
    tips: {
      deprecated: [];
      remainedActive: IMetagraphL0Tip[];
    };
    version: '0.0.1';
  };
  proofs: IMetagraphL0Proof[];
};
