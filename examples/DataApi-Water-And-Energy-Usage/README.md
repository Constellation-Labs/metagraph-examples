
#   Metagraph - Data API: Water and Energy Usage

This example utilizes the Data API to showcase a fundamental IoT use case. In this scenario, a client acting as a sensor transmits signed data updates to a metagraph. The metagraph verifies the data's integrity before integrating the updates into a snapshot state.

Here's an overview of how the example operates:

-   Each device sends requests to update water and/or energy usage.
-   Transactions must be unique based on the timestamp.

## Template

Primary code for the example can be found in the following files:



`modules/l0/src/main/scala/com/my/currency/l0/*`



`modules/l1/src/main/scala/com/my/currency/l1/*`



`modules/data_l1/src/main/scala/com/my/currency/data_l1/*`



`modules/shared_data/src/main/scala/com/my/currency/shared_data/*`

### Application Lifecycle

The methods of the DataApplication are:

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
-   GET `/data-application/addresses`: Lists all devices within the state.
-   GET `/data-application/addresses/${address}`: Displays information about the device associated with the provided address, or indicates "Not Found".
-   GET `/data-application/addresses/${address}/transactions`: Lists all transactions for a device up to the current snapshot. This method utilizes chaining. In other words, it retrieves the lastSnapshotOrdinal of each snapshot associated with the provided address having transactions, and then acquires the transactions of this snapshot.


## Scripts

This example encompasses a script for generating, signing, and transmitting data updates to the metagraph in `scripts/send_data_transaction.js`. This straightforward script requires the `globalL0Url` and `metagraphL1DataUrl` to match your metagraph's configuration. Additionally, you'll need to provide a private key representing the device (client) sending the transaction. This key is used for signing the transaction and logging into your network wallet.

### Usage

1.  Assuming you have Node.js installed, navigate to the directory and execute: `npm i`.
2.  Substitute your own values for the `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` variables.
3.  Run the script using `node send_data_transaction.js`.
4.  To observe the updated state after each update, query the state GET endpoint at `<your L1 base url>/data-application/addresses`.
