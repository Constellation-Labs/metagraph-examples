# Metagraph NFTs UI App

This app demonstrates demonstrates basic NFT functionality using the Constellation Metagraph Data API. This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

## Available Scripts

In the project directory, you can run:

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

Lets you create a new NFT inside a collection, collection IDs are the hashes returned by the cluster when creating a collection.

### `yarn script scripts/mint-collection.ts`

```
Options:
  -k, --account-pk <account-pk>            Account private key to use (hex)
  -n, --collection-name <collection-name>  Collection Name (must be unique)
  -h, --help                               display help for command
```

Lets you create a new collection inside the cluster, collection names must be unique.

### `yarn script scripts/mint-sample-collection.ts`

```
Options:
  -k, --account-pk <account-pk>            Account private key to use (hex)
  -n, --collection-name <collection-name>  Collection Name (must be unique)
  -c, --nft-count <nft-count>              NFT count to mint (integer)
  -h, --help                               display help for command
```

Lets you create a new collection inside the cluster, populates the collection with new NFTs based on the `--nft-count` argument.

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

Transfers an existing NFT to a new owner.

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

### `yarn start`

Runs the app in the development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.\
You will also see any lint errors in the console.

### `yarn build`

Builds the app for production to the `build` folder.\
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.\
Your app is ready to be deployed!

See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.

### `yarn eject`

**Note: this is a one-way operation. Once you `eject`, you can’t go back!**

If you aren’t satisfied with the build tool and configuration choices, you can `eject` at any time. This command will remove the single build dependency from your project.

Instead, it will copy all the configuration files and the transitive dependencies (webpack, Babel, ESLint, etc) right into your project so you have full control over them. All of the commands except `eject` will still work, but they will point to the copied scripts so you can tweak them. At this point you’re on your own.

You don’t have to ever use `eject`. The curated feature set is suitable for small and middle deployments, and you shouldn’t feel obligated to use this feature. However we understand that this tool wouldn’t be useful if you couldn’t customize it when you are ready for it.

## Action Encoding & Bad Signatures

Due to the nature of how signatures are validated by the nodes is particularly important to make sure when you encode your action structures you encode members in the order they appear on different examples/places like [nft_actions.ts](https://github.com/Constellation-Labs/metagraph-examples/blob/6c245fe6be97b9494db38fb0658909627a1688bf/examples/DataApi-NFT/sample-ui/scripts/types/nft_actions.ts), if you don't encode members as they are listed on the code you may get errors related to invalid signatures.

## Cluster Propagation & Changes

Some times Euclid nodes will need to propagate changes among the cluster members completely for an operation to be valid, so if for example you're creating a collection and then and NFT inside, make sure the collection is correctly created (give it time to propagate) so you don't get errors like "CollectionDoesNotExist".

## Learn More

You can learn more in the [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).

To learn React, check out the [React documentation](https://reactjs.org/).
