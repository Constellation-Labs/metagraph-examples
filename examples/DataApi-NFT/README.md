
# Metagraph - Data API for NFT Management
  

This example demonstrates an example use case of NFTs (Non-Fungible Tokens) based on the ERC-721 standard. Note that this shows a custom implementation of NFTs within a single metagraph using the Data API. It is not meant to describe a cross-metagraph standard for Constellation Network NFTs. 

 
- Minting NFT Collections

- Minting NFTs

- Transferring NFT Collections

- Transferring NFTs

  

In addition to these operations, the client can also fetch the following information:

  

- Retrieve all NFT collections

- Fetch a specific collection by ID

- Access all NFTs within a collection

- Fetch a specific NFT within a collection by ID

- Find all collections owned by a specific address

- Find all NFTs owned by a specific address

  
To fetch the information above, the base URL is: http://your_ip:your_port/data-application/${path}
e.g, https://localhost:8000/data-application/collections

Updates involving NFTs and collections undergo validation before being applied to the state.

  

## How the NFT System Works
  

The NFT management system operates as follows:

  

- Users can create collections associated with their signing address.

- Users can mint NFTs and assign them to a specific collection.

- Collection owners can transfer ownership of their collections to other wallets.

- NFT owners can transfer ownership of individual NFTs to other wallets.

- Each collection is unique.

- Each NFT within a collection is unique.

- Only owners can transfer ownership of collections and NFTs.

- NFT URIs must be unique.



## Template

Primary code for the example can be found in the following files:



`modules/l0/src/main/scala/com/my/currency/l0/*`



`modules/l1/src/main/scala/com/my/currency/l1/*`



`modules/data_l1/src/main/scala/com/my/currency/data_l1/*`



`modules/shared_data/src/main/scala/com/my/currency/shared_data/*`



### Application Lifecycle

The methods of the DataApplication are invoked in the following sequence:

-   `validateUpdate`
-   `validateData`
-   `combine`
-   `dataEncoder`
-   `dataDecoder`
-   `calculatedStateEncoder`
-   `signedDataEntityDecoder`
-   `serializeBlock`
-   `deserializeBlock`
-   `serializeState`
-   `deserializeState`
-   `serializeUpdate`
-   `deserializeUpdate`
-   `setCalculatedState`
-   `getCalculatedState`
-   `hashCalculatedState`
-   `routes`

For a more detailed understanding, please refer to the [complete documentation](https://docs.constellationnetwork.io/sdk/frameworks/currency/data-api) on the Data API.

### Lifecycle Functions

#### -> `validateUpdate`

* This method initiates the initial validation of updates on the L1 layer. Due to a lack of contextual information (state), its validation capabilities are constrained. Any errors arising from this method result in a 500 response from the `/data` POST endpoint.

#### -> `validateData`

* This method validates data on the L0 layer, with access to contextual information, including the current state. In this example, we ensure that the provided address matches the one that signed the message. Additionally, we verify the most recent update timestamp to prevent the acceptance of outdated or duplicated data.

#### -> `combine`

* This method takes validated data and the prior state, combining them to produce the new state. In this instance, we update device information in the state based on the validated update.

#### -> `dataEncoder` and `dataDecoder`

* These are the encoder/decoder components used for incoming updates.

#### -> `calculatedStateEncoder`

* This encoder is employed for the calculatedState.

#### -> `signedDataEntityDecoder`

* This function handles the parsing of request body formats (JSON, string, xml) into a `Signed[Update]` class.

#### -> `serializeBlock` and `deserializeBlock`

* The serialize function accepts the block object and converts it into a byte array for storage within the snapshot. The deserialize function is responsible for deserializing into Blocks.

#### -> `serializeState` and `deserializeState`

* The serialize function accepts the state object and converts it into a byte array for storage within the snapshot. The deserialize function is responsible for deserializing into State.

#### -> `serializeUpdate` and `deserializeUpdate`

* The serialize function accepts the update object and converts it into a byte array for storage within the snapshot. The deserialize function is responsible for deserializing into Updates.

#### -> `setCalculatedState`

* This function sets the calculatedState. You can store this as a variable in memory or use external services such as databases. In this example, we use in-memory storage.

#### -> `getCalculatedState`

* This function retrieves the calculated state.

#### -> `hashCalculatedState`

* This function creates a hash of the calculatedState to be validated when rebuilding this state, in case of restarting the metagraph.

#### -> `routes`

Customizes routes for our application.

In this example, the following endpoints are implemented:
- `GET <metagraph l0 url>/data-application/collections`: Retrieves all collections.
- `GET <metagraph l0 url>/data-application/collections/:collection_id`: Retrieves a collection by ID.
- `GET <metagraph l0 url>/data-application/collections/:collection_id/nfts`: Retrieves all NFTs of a collection.
- `GET <metagraph l0 url>/data-application/collections/:collection_id/nfts/:nft_id`: Retrieves an NFT of a collection by ID.
- `GET <metagraph l0 url>/data-application/addresses/:address/collections`: Retrieves all collections associated with an address.
- `GET <metagraph l0 url>/data-application/addresses/:address/nfts`: Retrieves all NFTs associated with an address.

## Scripts

The example includes a script named `send_data_transaction.js` for generating, signing, and sending data updates to the Metagraph. To use the script:

1. Install Node.js if not already done: `npm install`.

2. Replace `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` with your values.

3. Execute the script: `node send_data_transaction.js`.

4. Query the state GET endpoint at `<your L1 base url>/data-application/collections` to observe the updated state after each update.