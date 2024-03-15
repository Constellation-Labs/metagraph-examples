/* eslint-disable @typescript-eslint/no-var-requires */
/* eslint-disable no-undef */
/* config-overrides.js */

const path = require('path');

const {
  override,
  addWebpackModuleRule,
  babelInclude,
  addWebpackPlugin,
  addWebpackResolve
} = require('customize-cra');
const webpack = require('webpack');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');

/**
 * @param {import('webpack').Configuration} config
 * @param {*} env
 * @returns
 */
module.exports = function (config, env) {
  //console.dir(config, { depth: 10 });

  const FrontendAppEnvironment = {};

  for (const [key, value] of Object.entries(process.env)) {
    if (key.startsWith('REACT_APP_')) {
      FrontendAppEnvironment[key] = value;
    }
  }

  const updateConfig = override(
    addWebpackModuleRule({
      test: /\.scss$/,
      use: [
        'style-loader',
        'css-loader',
        {
          loader: 'sass-loader',
          options: {
            sassOptions: { includePaths: [path.join(__dirname, 'src')] }
          }
        }
      ]
    }),
    babelInclude([path.resolve(__dirname, '..')]),
    addWebpackPlugin(
      new webpack.DefinePlugin({
        FrontendAppEnvironment: `JSON.parse('${JSON.stringify(
          FrontendAppEnvironment
        )}')`,
        'process.env.FRONTEND_APP_ENVIRONMENT': true
      })
    ),
    addWebpackResolve({ plugins: [new TsconfigPathsPlugin()] })
  );

  config = updateConfig(config);

  console.dir(config, { depth: 10 });

  return config;
};
