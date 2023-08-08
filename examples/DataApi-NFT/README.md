
# Metagraph - Data API for NFT Management
  

This example demonstrates a fundamental use case of NFTs (Non-Fungible Tokens) using the Data API provided by the Metagraph platform. The example showcases various operations that a client can perform:

 
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

  

The primary code for this example is located in the following files:

  

- `modules/l0/src/main/scala/com/my/currency/l0/Main.scala`

- `modules/l1/src/main/scala/com/my/currency/l1/Main.scala`

- `modules/data_l1/src/main/scala/com/my/currency/data_l1/Main.scala`

- `modules/shared_data/src/main/scala/com/my/currency/shared_data/MainData.scala`

  

### Application Lifecycle

  

The DataApplication methods are executed in the following order:

  

1. `serializeUpdate`

2. `validateUpdate`

3. `validateData`

4. `combine`

5. `serializeState`

  

For additional details, refer to the [full documentation](https://docs.constellationnetwork.io/sdk/frameworks/currency/data-api) of the Data API.

  

### shared_data

  

- `validateUpdate`: Initial validation on the update at the L1 layer. Checks include uniqueness of collections, uniqueness of NFTs within a collection, and uniqueness of NFT URIs.

- `validateData`: Validation on data at the L0 layer with access to context information. Similar checks as in `validateUpdate`.

- `combine`: Accepts validated data and the previous state to compute the new state. In this example, it creates new collections, NFTs, and handles transfers.

- `serializeState`: Serializes the State object to a byte array for storage in the snapshot.

- `serializeUpdate`: Serializes the Update object to a byte array. Updates are serialized before input signature validation.

- `deserializeState` and `deserializeUpdate`: Opposite of `serializeState` and `serializeUpdate`, used for deserialization.

  

### l0

  

The L0 module contains the `genesis` function, initializing the state with an empty `polls` map. The Data class (shared_data) implements the remaining methods.

  

### l1

  

The currency L1 layer, with a default implementation in this example.

  

### data_l1

  

This module processes incoming data requests. It includes the `/data` POST endpoint for data requests. The remaining methods are implemented in the Data class (shared_data).

  

## Scripts

  

The example includes a script named `send_data_transaction.js` for generating, signing, and sending data updates to the Metagraph. To use the script:

  

1. Install Node.js if not already done: `npm install`.

2. Replace `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` with your values.

3. Execute the script: `node send_data_transaction.js`.

4. Query the state GET endpoint at `<your L1 base url>/data-application/addresses` to observe the updated state after each update.