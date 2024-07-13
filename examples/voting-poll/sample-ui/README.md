# Sample UI Project - Voting App

## Description

This is a demo application built using Next.js 14 and npm. The application allows users to create polls and vote on them using their Constellation DAG wallet. Users can only vote if they have a balance in their wallet.


## Features

+ Create new polls
+ Vote on existing polls
+ Authentication via Constellation DAG wallet
+ Check wallet balance before voting
  
## Prerequisites

+ Node.js (v18 or higher)
+ npm (v9 or higher)
+ Stargazer DAG wallet

## Usage

### Development server

1. Install dependencies:

```sh
npm install
```

2. start the development server:

```sh
npm run dev
```

Open http://localhost:5431 with your browser to see the result.


## Stargazer wallet dependency

### Wallet balance
Users need to have a balance in their DAG wallet to vote. The application checks the wallet balance before allowing a vote.

#### Adding balance via genesis file
To set up a wallet with balance, you can modify the genesis file on euclid-development-environment located at `euclid-development-environment/source/metagraph-l0/genesis/genesis.csv`. 

The file is a csv file, first column indicates the target address for funds, the second column indicates how much vp to allocate for the wallet (remember this value is raw value, so the actual value would be (rawValue/1e8)).

Re-build tessellation images to see the reflected balance on the network.