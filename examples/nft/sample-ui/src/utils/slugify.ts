import { stringFormat } from './index.ts';

const slugify = (str: string): string => stringFormat(str, 'SLUGIFY');

export { slugify };
