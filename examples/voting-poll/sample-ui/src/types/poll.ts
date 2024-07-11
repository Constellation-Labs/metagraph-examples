export type IPoll = {
  id: string;
  name: string;
  owner: string;
  pollOptions: string[];
  startSnapshotOrdinal: number;
  endSnapshotOrdinal: number;
  results?: { option: string; votes: number }[];
};
