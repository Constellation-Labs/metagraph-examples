
# Metagraph - Data API - Voting Poll

  

This example demonstrates a basic voting poll use case using the Data API. In the example, a client can send two types of signed data updates to a metagraph: one to create a vote poll and another to vote in a poll. These updates are validated before being merged into the snapshot state.

Here's how the voting system works:

+ Each wallet can only vote once per poll.
+ If a voting wallet does not have any balance, the vote will be disregarded. The user can vote again with the same wallet once they replenish its balance.
+ We verify the selected option to ensure its validity.
+ The voting weight is proportional to the balance in the wallet. For instance, if a wallet has a balance of 100 tokens, a vote from this wallet will increment the tally of the selected option by 100.
+ The system prevents the creation of duplicate polls.
## Template

Primary code for the example can be found in the following files:

  

`modules/l0/src/main/scala/com/my/currency/l0/Main.scala`

  

`modules/l1/src/main/scala/com/my/currency/l1/Main.scala`

  

`modules/data_l1/src/main/scala/com/my/currency/data_l1/Main.scala`

  

`modules/shared_data/src/main/scala/com/my/currency/shared_data/MainData.scala`

  

### Application Lifecycle

The DataApplication methods are called in the following order:

- `serializeUpdate`

- `validateUpdate`

- `validateData`

- `combine`

- `serializeState`

  

See [full documentation](https://docs.constellationnetwork.io/sdk/frameworks/currency/data-api) on the Data API for additional detail.

  

### shared_data

  

-> `validateUpdate`

  

This method performs initial validation on the update on the L1 layer.  For this example, we check that the provided address is the same as the address that signed the message. We also check if the user already voted in a poll, if the poll already exists, and if the poll is valid yet, comparing the current snapshot with the end snapshot, provided on the poll creation. If an error is returned from this method, it will be returned as a 500 response from the `/data` POST endpoint.

  

-> `validateData`

  

This method performs validation on the data on the L0 layer and has access to context information. For this example, we check that the provided address is the same as the address that signed the message. We also check if the user already voted in a poll, if the poll already exists, and if the poll is valid yet, comparing the current snapshot with the end snapshot, provided on the poll creation.

  

-> `combine`

  

This method accepts validated data and the previous state which is combined to return the new state. For this example, we update inserting a new poll or voting in an existent poll, storing the information of who voted and what is the vote with amount.

  

-> `serializeState`

  

This method accepts the State object and serializes it to a byte array for storage in the snapshot.

  

-> `serializeUpdate`

  

This method accepts the Update object and serializes it to a byte array. Updates are serialized before their input signatures are checked for validity.

  
  

-> `deserializeState` and `deserializeUpdate`

  

The opposite of `serializeState` and `serializeUpdate`, this will deserialize to State and Update objects, respectively.

  

### l0

  

The L0 module contains the `genesis` function. This function will initialize our state, in this case, we initialize with an empty `polls` map. The remaining methods are implemented in the Data class (shared_data).

  

### l1

  

The currency L1 layer, in this example we use a default implementation.

  

### data_l1

  

The module that will receive the requests to be processed. This module contains the `/data` POST endpoint which accepts incoming data requests.

  

The remaining methods are implemented in the Data class (shared_data).

  

## Scripts

  

This example includes a script to generate, sign, and send data updates to the metagraph in `scripts/send_data_transaction.js`. This is a simple script where you must provide the `globalL0Url` and the `metagraphL1DataUrl` to match the configuration of your metagraph. You also must provide a private key representing the user that will create or vote in a poll (client) that is sending the transaction, this key will be used to sign the transaction and to log in your wallet to the network.

  

### Usage

- With node installed, move to the directory and then type: `npm i`.

- Replace the `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` variables with your values.

- Run the script with `node send_data_transaction.js`

- Query the state GET endpoint at `<your L1 base url>/data-application/addresses` to see the updated state after each update.
