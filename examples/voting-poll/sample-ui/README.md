# Sample UI Project - Voting App

## Description

This is a demo web app frontend built using Next.js 14 and npm. The application allows users to create polls and vote on them using their Stargazer wallet. The metagraph is used as a backend for this web app.


## Features

+ Create new polls
+ Vote on existing polls
+ Authentication via Stargazer wallet
+ Check wallet balance before voting
  
## Prerequisites

+ Node.js (v18 or higher)
+ npm (v9 or higher)
+ Stargazer wallet

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

Open http://localhost:5431 with your browser to see the result. _Note, ensure that your metagraph is running using hydra status before opening this app. It will fail if the metagraph is offline._


## Stargazer wallet dependency

### Wallet balance
Users need to have a balance in their Stargazer wallet to vote. The application checks the wallet balance before allowing a vote.

#### Adding token to Stargazer
In order to see metagraph tokens in your Stargazer wallet, you will need to add the metagraph as a custom Constellation token in your wallet. 

Open Stargazer wallet in Chrome, scroll to the bottom and click "manage tokens". Click the "+" in the upper right of the screen to add a custom token. 

Make sure your metagraph is started with `hydra start-genesis` and note the endpoint details output to your screen. You can also find these details using `hydra status`. 

Use the following details from your metagraph output:
- Network: Constellation
- L1 endpoint: metagraph-node-1 Currency L1 (http://localhost:9300)
- L0 endpoint: metagraph-node-1 Metagraph L0 (http://localhost:9200) 
- Metagraph ID: metagraph ID from your output
- Token name: Anything you want
- Token symbol: Anything you want. 


#### Adding balance via genesis file
To set up a wallet with balance, you can modify the genesis file in your local Euclid project located at `source/metagraph-l0/genesis/genesis.csv`. 

The file is a csv file, first column indicates the target address for funds, the second column indicates the token balance to allocate for the wallet. This balance is denominated in datum, so the value added to the file should 1e8 times larger than number of tokens you want to create (e.g. 55 tokens would be 5500000000). 

Restart your metagraph from genesis to see the balance reflected in your wallet: `hydra start-genesis`, you may need to rebuild your images again. 