type ICachedValue<T> = { value: T; expiration: number };

const DefaultNamespace = 'general';

const cachedValue = async <V>(
  nskey: string,
  onNotFound: () => Promise<ICachedValue<V>>,
  target: 'local' | 'session' = 'local'
): Promise<V> => {
  const keyparts = nskey.split(':');
  keyparts.length === 1 && keyparts.unshift(DefaultNamespace);

  const [ns, key] = keyparts;

  const targetStorage =
    target === 'local' ? window.localStorage : window.sessionStorage;

  const collection: Record<string, ICachedValue<any>> = JSON.parse(
    targetStorage.getItem(ns) ?? '{}'
  );

  let item = collection[key];
  if (!item || item.expiration < Date.now()) {
    item = await onNotFound();

    collection[key] = item;
    targetStorage.setItem(ns, JSON.stringify(collection));
  }

  return item.value;
};

const cachedValueOrNull = <V = any>(
  nskey: string,
  target: 'local' | 'session' = 'local'
): V | null => {
  const keyparts = nskey.split(':');
  keyparts.length === 1 && keyparts.unshift(DefaultNamespace);

  const [ns, key] = keyparts;

  const targetStorage =
    target === 'local' ? window.localStorage : window.sessionStorage;

  const collection: Record<string, ICachedValue<any>> = JSON.parse(
    targetStorage.getItem(ns) ?? '{}'
  );

  const item = collection[key];
  if (!item || item.expiration < Date.now()) {
    return null;
  }

  return item.value;
};

const setCachedValue = (
  nskey: string,
  target: 'local' | 'session' = 'local',
  value: any,
  expiration: number
): void => {
  const keyparts = nskey.split(':');
  keyparts.length === 1 && keyparts.unshift(DefaultNamespace);

  const [ns, key] = keyparts;

  const targetStorage =
    target === 'local' ? window.localStorage : window.sessionStorage;

  const collection: Record<string, ICachedValue<any>> = JSON.parse(
    targetStorage.getItem(ns) ?? '{}'
  );

  collection[key] = { value, expiration };
  targetStorage.setItem(ns, JSON.stringify(collection));
};

const deleteCachedValue = (
  nskey: string,
  target: 'local' | 'session' = 'local'
): void => {
  const keyparts = nskey.split(':');
  keyparts.length === 1 && keyparts.unshift(DefaultNamespace);

  const [ns, key] = keyparts;

  const targetStorage =
    target === 'local' ? window.localStorage : window.sessionStorage;

  const collection: Record<string, ICachedValue<any>> = JSON.parse(
    targetStorage.getItem(ns) ?? '{}'
  );

  delete collection[key];
  targetStorage.setItem(ns, JSON.stringify(collection));
};

export { cachedValue, cachedValueOrNull, setCachedValue, deleteCachedValue };
