/** @type {import('next').NextConfig} */
const nextConfig = {
  webpack: (config) => {
    config.module.rules.push({
      test: /\.svg$/,
      use: ['@svgr/webpack']
    });

    return config;
  },
  redirects: async () => {
    return [{ source: '/', destination: '/polls', permanent: false }];
  }
};

export default nextConfig;
