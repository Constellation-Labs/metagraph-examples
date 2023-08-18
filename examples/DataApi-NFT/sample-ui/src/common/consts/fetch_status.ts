enum FetchStatus {
  IDLE = 'FetchStatus(IDLE)',
  PENDING = 'FetchStatus(PENDING)',
  DONE = 'FetchStatus(DONE)',
  NOT_FOUND = 'FetchStatus(NOT_FOUND)',
  SERVER_ERROR = 'FetchStatus(SERVER_ERROR)',
  ERROR = 'FetchStatus(ERROR)'
}

type FetchStatusValue<T> = {
  status: FetchStatus;
  value: T;
};

const isFetchStatus = (value: any): value is FetchStatus =>
  typeof value === 'string' &&
  Object.values<string>(FetchStatus).includes(value);

const isFetchStatusValue = (value: any): value is FetchStatusValue<any> => {
  if (typeof value !== 'object') {
    return false;
  }

  if (value === null) {
    return false;
  }

  if (!('status' in value && isFetchStatus(value.status))) {
    return false;
  }

  if (!('value' in value)) {
    return false;
  }

  return true;
};

export {
  type FetchStatusValue,
  FetchStatus,
  isFetchStatus,
  isFetchStatusValue
};
