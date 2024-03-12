/* eslint-disable no-undef */
/**
 * @type {import('eslint').Linter.Config}
 */
const config = {
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:prettier/recommended',
    'plugin:import/errors',
    'plugin:import/warnings',
    'plugin:import/typescript'
  ],
  parser: '@typescript-eslint/parser',
  plugins: ['@typescript-eslint', 'unused-imports'],
  root: true,
  rules: {
    'no-control-regex': 'off',
    '@typescript-eslint/no-unnecessary-type-constraint': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    'no-async-promise-executor': 'off',
    'import/no-unresolved': 'off',
    'import/order': [
      'error',
      {
        'newlines-between': 'always',
        groups: [
          'builtin',
          'external',
          'internal',
          'parent',
          'sibling',
          'index'
        ],
        pathGroups: [],
        pathGroupsExcludedImportTypes: []
      }
    ],
    'import/no-duplicates': 'error',
    'import/no-useless-path-segments': [
      'error',
      {
        noUselessIndex: false
      }
    ],
    'unused-imports/no-unused-imports': 'error',
    'import/namespace': ['error', { allowComputed: true }],
    'react/display-name': ['off']
  },
  settings: {
    'import/internal-regex': '^@(.+)?$|^@public(.+)?$|^@/(.+)?$'
  }
};

module.exports = config;
