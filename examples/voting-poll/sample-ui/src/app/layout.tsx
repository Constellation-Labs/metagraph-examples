import '../styles/globals.scss';
import '../styles/fonts.scss';
import 'react-toastify/dist/ReactToastify.css';

import type { Metadata } from 'next';
import { ToastContainer } from 'react-toastify';

import { Header } from '../components';
import { WalletProvider } from '../providers';

export const metadata: Metadata = {
  title: 'Voting Poll Example'
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <head>
        <link
          rel="apple-touch-icon"
          sizes="180x180"
          href="/apple-touch-icon.png"
        />
        <link
          rel="icon"
          type="image/png"
          sizes="32x32"
          href="/favicon-32x32.png"
        />
        <link
          rel="icon"
          type="image/png"
          sizes="16x16"
          href="/favicon-16x16.png"
        />
        <link rel="manifest" href="/site.webmanifest" />
        <link rel="mask-icon" href="/safari-pinned-tab.svg" color="#000000" />
        <meta name="msapplication-TileColor" content="#00aba9" />
        <meta name="theme-color" content="#000000" />
      </head>
      <body>
        <WalletProvider>
          <Header />
          {children}
          <ToastContainer position="bottom-right" pauseOnHover />
        </WalletProvider>
      </body>
    </html>
  );
}
