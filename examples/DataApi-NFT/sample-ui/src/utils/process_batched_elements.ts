const processBatchedArray = async <T>(
  array: T[],
  batchSize: number,
  processor: (
    records: T[],
    batchNumber: number,
    totalBatches: number,
    totalCount: number
  ) => Promise<boolean>
) => {
  const totalCount = array.length;
  const totalBatches = Math.ceil(totalCount / batchSize);

  for (let batchNumber = 0; batchNumber < totalBatches; batchNumber++) {
    const records = array.slice(
      batchNumber * batchSize,
      batchNumber * batchSize + batchSize
    );

    const response = await processor(
      records,
      batchNumber,
      totalBatches,
      totalCount
    );

    if (!response) {
      break;
    }
  }
};

const processBatchedCount = async (
  startNumber: number,
  endNumber: number,
  batchSize: number,
  processor: (
    range: { start: number; end: number },
    batchNumber: number,
    totalBatches: number,
    totalCount: number
  ) => Promise<boolean>
) => {
  const totalCount = endNumber - startNumber;
  const totalBatches = Math.ceil(totalCount / batchSize);

  for (let batchNumber = 0; batchNumber < totalBatches; batchNumber++) {
    const range = {
      start: startNumber + batchNumber * batchSize,
      end: Math.min(
        startNumber + batchNumber * batchSize + batchSize,
        endNumber
      )
    };

    const response = await processor(
      range,
      batchNumber,
      totalBatches,
      totalCount
    );

    if (!response) {
      break;
    }
  }
};

export { processBatchedArray, processBatchedCount };
