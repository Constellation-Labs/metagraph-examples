export const shorten = (
  value: string,
  prefixLength = 5,
  suffixLength = 5,
  separator = '...'
) =>
  value.slice(0, prefixLength) +
  separator +
  value.slice(value.length - suffixLength);
