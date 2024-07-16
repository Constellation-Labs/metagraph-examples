export type IPoll = {
  id: string;
  name: string;
  owner: string;
  result: Record<string, number>;
  startSnapshotOrdinal: number;
  endSnapshotOrdinal: number;
  status: 'Closed' | 'Open';
};
