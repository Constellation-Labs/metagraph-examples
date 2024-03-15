class FlushableBuffer<T> {
  private buffer: T[];

  constructor(
    private size: number,
    private onFlush: (buffer: T[]) => Promise<any>
  ) {
    this.buffer = [];
  }

  async push(element: T) {
    this.buffer.push(element);
    if (this.buffer.length >= this.size) {
      await this.flush();
    }
  }

  async flush() {
    if (this.buffer.length === 0) {
      return;
    }

    await this.onFlush([...this.buffer]);
    this.buffer = [];
  }
}

const processInFlushableBuffer = async <T>(
  elements: T[],
  bufferSize: number,
  onFlush: (buffer: T[]) => Promise<any>,
  onElement: (
    element: T,
    pushElement: (element: T) => Promise<void>
  ) => Promise<void>
) => {
  const buffer = new FlushableBuffer<T>(bufferSize, onFlush);

  for (const element of elements) {
    await onElement(element, (element) => buffer.push(element));
  }

  buffer.flush();
};

export { FlushableBuffer, processInFlushableBuffer };
