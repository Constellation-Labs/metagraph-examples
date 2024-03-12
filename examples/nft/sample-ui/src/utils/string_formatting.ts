/**
 * Transformed Idea
 * https://gist.github.com/codeguy/6684588
 */

const StringFormaters = {
  NORMALIZE: (str: string) => {
    // remove accents, swap ñ for n, etc
    const from = 'àáäâèéëêìíïîòóöôùúüûñç';
    const to = 'aaaaeeeeiiiioooouuuunc';

    for (let i = 0, l = from.length; i < l; i++) {
      str = str.replace(new RegExp(from.charAt(i), 'gi'), to.charAt(i));
    }

    return str.normalize('NFD').replace(/\p{Diacritic}/gu, '');
  },
  CAMEL_CASE: (str: string) => {
    str = StringFormaters.NORMALIZE(str);
    return str
      .replace(/[^a-z0-9 -]/gi, '') // remove invalid chars
      .replace(/\w+/g, function (word, index) {
        return index === 0
          ? word.toLowerCase()
          : word.charAt(0).toUpperCase() + word.substring(1).toLowerCase();
      })
      .replace(/\s+/g, '');
  },
  PASCAL_CASE: (str: string) => {
    str = StringFormaters.NORMALIZE(str);
    return str
      .replace(/[^a-z0-9 -]/gi, '') // remove invalid chars
      .replace(/(\w)(\w*)/g, function (g0, g1, g2) {
        return g1.toUpperCase() + g2.toLowerCase();
      })
      .replace(/\s+/g, '');
  },
  SNAKE_CASE: (str: string) => {
    str = StringFormaters.NORMALIZE(str);
    return str
      .replace(/[^a-z0-9 -]/gi, '') // remove invalid chars
      .replace(/\s+/g, '_')
      .toLowerCase();
  },
  SLUGIFY: (str: string) => {
    str = StringFormaters.NORMALIZE(str);
    return str
      .trim()
      .toLowerCase()
      .replace(/[^a-z0-9 -]/gi, '') // remove invalid chars
      .replace(/\s+/g, '-') // collapse whitespace and replace by -
      .replace(/-+/g, '-'); // collapse dashes
  }
} as const;

const stringFormat = (
  str: string,
  format: keyof typeof StringFormaters = 'SLUGIFY'
) => StringFormaters[format](str);

export { StringFormaters, stringFormat };
