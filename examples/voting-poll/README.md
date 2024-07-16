# Metagraph - Data API - Voting Poll

This example demonstrates a basic voting poll use case using the Data API. In the example, a client can send two types of signed data updates to a metagraph: one to create a vote poll and another to vote in a poll. These updates are validated before being merged into the snapshot state.

Here's how the voting system works:

- Each wallet can only vote once per poll.
- If a voting wallet does not have any balance, the vote will be disregarded. The user can vote again with the same wallet once they replenish its balance.
- We verify the selected option to ensure its validity.
- The voting weight is proportional to the balance in the wallet. For instance, if a wallet has a balance of 100 tokens, a vote from this wallet will increment the tally of the selected option by 100.
- The system prevents the creation of duplicate polls.

## Template

Primary code for the example can be found in the following files:

`modules/l0/src/main/scala/com/my/currency/l0/*`

`modules/l1/src/main/scala/com/my/currency/l1/*`

`modules/data_l1/src/main/scala/com/my/currency/data_l1/*`

`modules/shared_data/src/main/scala/com/my/currency/shared_data/*`

### Application Lifecycle

The methods of the DataApplication are invoked in the following sequence:

- `validateUpdate`
- `validateData`
- `combine`
- `dataEncoder`
- `dataDecoder`
- `calculatedStateEncoder`
- `signedDataEntityDecoder`
- `serializeBlock`
- `deserializeBlock`
- `serializeState`
- `deserializeState`
- `serializeUpdate`
- `deserializeUpdate`
- `setCalculatedState`
- `getCalculatedState`
- `hashCalculatedState`
- `routes`

For a more detailed understanding, please refer to the [complete documentation](https://docs.constellationnetwork.io/sdk/frameworks/currency/data-api) on the Data API.

### Lifecycle Functions

#### -> `validateUpdate`

- This method initiates the initial validation of updates on the L1 layer. Due to a lack of contextual information (state), its validation capabilities are constrained. Any errors arising from this method result in a 500 response from the `/data` POST endpoint.

#### -> `validateData`

- This method validates data on the L0 layer, with access to contextual information, including the current state. In this example, we ensure that the provided address matches the one that signed the message. Additionally, we verify the most recent update timestamp to prevent the acceptance of outdated or duplicated data.

#### -> `combine`

- This method takes validated data and the prior state, combining them to produce the new state. In this instance, we update device information in the state based on the validated update.

#### -> `dataEncoder` and `dataDecoder`

- These are the encoder/decoder components used for incoming updates.

#### -> `calculatedStateEncoder`

- This encoder is employed for the calculatedState.

#### -> `signedDataEntityDecoder`

- This function handles the parsing of request body formats (JSON, string, xml) into a `Signed[Update]` class.

#### -> `serializeBlock` and `deserializeBlock`

- The serialize function accepts the block object and converts it into a byte array for storage within the snapshot. The deserialize function is responsible for deserializing into Blocks.

#### -> `serializeState` and `deserializeState`

- The serialize function accepts the state object and converts it into a byte array for storage within the snapshot. The deserialize function is responsible for deserializing into State.

#### -> `serializeUpdate` and `deserializeUpdate`

- The serialize function accepts the update object and converts it into a byte array for storage within the snapshot. The deserialize function is responsible for deserializing into Updates.

#### -> `setCalculatedState`

- This function sets the calculatedState. You can store this as a variable in memory or use external services such as databases. In this example, we use in-memory storage.

#### -> `getCalculatedState`

- This function retrieves the calculated state.

#### -> `hashCalculatedState`

- This function creates a hash of the calculatedState to be validated when rebuilding this state, in case of restarting the metagraph.

#### -> `routes`

Customizes routes for our application.

In this example, the following endpoints are implemented:

- GET `<metagraph l0 url>/data-application/polls`: Returns the polls.
- GET `<metagraph l0 url>/data-application/polls/:poll_id`: Returns the poll by id.

## Sample UI Project

This example comes with a sample UI web app demonstrating integration with Stargazer Wallet and interaction with the metagraph APIs. You can set it up and run it from the instructions in the [sample-ui](./sample-ui/README.md) folder.

## Scripts

This example includes a script to generate, sign, and send data updates to the metagraph in `scripts/send_data_transaction.js`. This is a simple script where you must provide the `globalL0Url` and the `metagraphL1DataUrl` to match the configuration of your metagraph. You also must provide a private key representing the user that will create or vote in a poll (client) that is sending the transaction, this key will be used to sign the transaction and to log in your wallet to the network.

### Usage

- With node installed, move to the directory and then type: `npm i`.

- Replace the `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` variables with your values.

- Run the script with `node send_data_transaction.js`

- Query the state GET endpoint at `<your metagraph L0 base url>/data-application/addresses` to see the updated state after each update.




