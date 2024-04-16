# Metagraph NFTs UI App

This simple web app provides a front end to view data stored on the metagraph. It provides scripts to help sign data and submit it to a metagraph node in the proper format. It's important to remember that this web app is just a view into the state stored on the metagraph using the custom API endpoints built into the mL0 layer. For debugging and learning purposes, it's highly recommended to focus on the outputs of the metagraph API endpoints rather than on this demo UI. 

See the metagraph L0 [CustomRoutes.scala](../template/modules/l0/src/main/scala/com/my/nft/l0/custom_routes/CustomRoutes.scala) file to understand how the API endpoints used in this app are constructed. 


Be sure to also examine the source code of the [scripts](./scripts) described below to better understand how to construct requests to the metagraph. 


## Installation
Install package dependencies with yarn

`yarn install`


## Running the Web App
Start the web app in development mode with yarn start. Open [http://localhost:6542](http://localhost:6542) to view it in the browser.

`yarn start`

## Wallets and Private Keys
All of the scripts below require a private key as an input parameter in order to sign the data sent to the metagraph. Private keys can be generated using [dag4.js](https://docs.constellationnetwork.io/hypergraph/dag4-wallets#interacting-with-wallets) or exported from [Stargazer wallet](https://play.google.com/store/apps/details?id=com.stargazer). 

You can find more information on [Accounts and Keys](https://docs.constellationnetwork.io/metagraphs/accounts) on Constellation Network in the docs.

## Available Scripts

### `yarn script scripts/mint-collection.ts`

```
Options:
  -k, --account-pk <account-pk>            Account private key to use (hex)
  -n, --collection-name <collection-name>  Collection Name (must be unique)
  -h, --help                               display help for command
```

This script mints a new collection into state. Collections have a hasMany relationship with NFTs - they can contain multiple or none. In this step, we create a collection with a name which will be owned by the wallet associated with the provided private key. 

After minting a collection, you can see the updated state at `http://YOUR_NODE_IP:PORT/data-application/collections` (e.g. `http://localhost:9400/data-application/collections`) or in the web app.


### `yarn script scripts/mint-collection-nft.ts`

```
Options:
  -k, --account-pk <account-pk>            Account private key to use (hex)
  -c, --collection-id <collection-id>      Collection Id (hash)
  -i, --nft-id <nft-id>                    NFT id (integer)
  -n, --nft-name <nft-name>                NFT name
  -d, --nft-description <nft-description>  NFT description
  -u, --nft-uri <nft-uri>                  NFT uri/image-url
  -o, --nft-owner <nft-owner>              NFT owner (address)
  -h, --help                               display help for command
```
This allows you to create a new NFT record inside an existing collection. 

The collection ID is returned after creating the collection, or it can be found via the `/data-application/collections` endpoint. 

NFT id, name, and description can be anything you choose. NFT URI should be a link to the NFT image, hosted somewhere externally. 

The NFT owner can be any DAG address. It can be the owner of the collection or a different address. 

After minting an NFT, you can view the updated state at: `http://YOUR_NODE_IP:PORT/data-application/collections/COLLECTION_ID/nfts` (e.g. `http://localhost:9400/data-application/collections/6237c03d8fc53711a4d0423d707f7deae84336fe0510ae66366de8e3321e00a7/nfts`) or in the web app UI.


### `yarn script scripts/mint-sample-collection.ts`

```
Options:
  -k, --account-pk <account-pk>            Account private key to use (hex)
  -n, --collection-name <collection-name>  Collection Name (must be unique)
  -c, --nft-count <nft-count>              NFT count to mint (integer)
  -h, --help                               display help for command
```

This helper script creates a new collection, then populates the collection with new sample NFTs based on the `--nft-count` argument.

_Note: This command may produce errors related to [Cluster Propagation & Changes](#cluster-propagation--changes)._

### `yarn script scripts/transfer-collection-nft.ts`

```
Options:
  -k, --account-pk <account-pk>        Account private key to use (hex)
  -c, --collection-id <collection-id>  Collection Id (hash)
  -n, --nft-id <nft-id>                NFT Id (integer)
  -f, --from-address <from-address>    From address (address)
  -t, --to-address <to-address>        To address (address)
  -h, --help                           display help for command
```

Transfers an existing NFT to a new owner. The `from` address is always the wallet address associated with the provided account-pk.

### `yarn script scripts/transfer-collection.ts`

```
Options:
  -k, --account-pk <account-pk>        Account private key to use (hex)
  -c, --collection-id <collection-id>  Collection Id (hash)
  -f, --from-address <from-address>    From address (address)
  -t, --to-address <to-address>        To address (address)
  -h, --help                           display help for command
```

Transfers an existing collection to a new owner.


## Action Encoding & Bad Signatures

Due to the nature of how signatures are validated by the nodes is particularly important to make sure when you encode your action structures you encode members in the order they appear on different examples/places like [nft_actions.ts](https://github.com/Constellation-Labs/metagraph-examples/blob/6c245fe6be97b9494db38fb0658909627a1688bf/examples/DataApi-NFT/sample-ui/scripts/types/nft_actions.ts), if you don't encode members as they are listed on the code you may get errors related to invalid signatures.

## Cluster Propagation & Changes

Sometimes Euclid nodes will need to propagate changes among the cluster members completely for an operation to be valid, so if for example you're creating a collection and then an NFT inside, make sure the collection is correctly created (give it time to propagate) so you don't get errors like "CollectionDoesNotExist".
