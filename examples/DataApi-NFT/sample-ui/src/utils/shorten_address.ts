const shortenAddress = (
  address: string,
  ellipsis = '...',
  chstart = 4,
  chend = 4
) => {
  return `${address.substring(0, chstart)}${ellipsis}${address.substring(
    address.length - chend,
    address.length
  )}`;
};

export { shortenAddress };
