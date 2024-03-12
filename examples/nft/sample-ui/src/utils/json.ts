const isValidJSON = (value: string) => {
  try {
    JSON.parse(value);
    return true;
  } catch (e) {
    return false;
  }
};

const isValidJSONStringifiable = (value: any) => {
  try {
    JSON.stringify(value);
    return true;
  } catch (e) {
    return false;
  }
};

const parseJSONOrFallback = (value: string, fallback: any) => {
  try {
    return JSON.parse(value);
  } catch (e) {
    return fallback;
  }
};

const stringifyJSONOrFallback = (value: any, fallback: any) => {
  try {
    return JSON.stringify(value);
  } catch (e) {
    return fallback;
  }
};

export {
  isValidJSON,
  parseJSONOrFallback,
  isValidJSONStringifiable,
  stringifyJSONOrFallback
};
