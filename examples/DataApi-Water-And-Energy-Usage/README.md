
#   Metagraph - Data API: Water and Energy Usage

This example utilizes the Data API to showcase a fundamental IoT use case. In this scenario, a client acting as a sensor transmits signed data updates to a metagraph. The metagraph verifies the data's integrity before integrating the updates into a snapshot state.

Here's an overview of how the example operates:

-   Each device sends requests to update water and/or energy usage.
-   Transactions must be unique based on the timestamp.

## Template

The primary code for this example can be located in the following files:

-   `modules/l0/src/main/scala/com/my/currency/l0/Main.scala`
-   `modules/l1/src/main/scala/com/my/currency/l1/Main.scala`
-   `modules/data_l1/src/main/scala/com/my/currency/data_l1/Main.scala`
-   `modules/shared_data/src/main/scala/com/my/currency/shared_data/Main.scala`

### Application Lifecycle

The DataApplication methods are invoked in the following sequence:

-   `serializeUpdate`
-   `validateUpdate`
-   `validateData`
-   `combine`
-   `serializeState`

For more detailed information, refer to the [complete documentation](https://docs.constellationnetwork.io/sdk/frameworks/currency/data-api) on the Data API.

### shared_data

-> `validateUpdate`

This method conducts initial validation of updates on the L1 layer. Due to a lack of context information (state), it's constrained in terms of validation capabilities. If an error arises from this method, it will be returned as a 500 response from the `/data` POST endpoint.

-> `validateData`

This method validates data on the L0 layer, having access to contextual information, including the current state. In this example, we ensure that the provided address matches the one that signed the message. We also verify the most recent update timestamp to prevent the acceptance of outdated or duplicated data.

-> `combine`

This method takes validated data and the prior state, combining them to yield the new state. In this instance, we update device information in the state based on the validated update.

-> `serializeState`

This method accepts the State object and converts it into a byte array for storage within the snapshot.

-> `serializeUpdate`

This method accepts the Update object and serializes it into a byte array. Updates are serialized prior to validating their input signatures.

-> `deserializeState` and `deserializeUpdate`

These are counterparts to `serializeState` and `serializeUpdate`, responsible for deserializing into State and Update objects, respectively.

### l0

The L0 module incorporates the `genesis` function, responsible for initializing our state. In this case, we initialize it with empty maps for `devices`, `transactions`, `lastTxnsRefs`, and `lastSnapshotRefs`. The remaining methods are implemented in the Data class (shared_data).

### l1

The currency L1 layer employs a default implementation in this example.

### data_l1

This module receives incoming data requests. It features the `/data` POST endpoint for this purpose. Additionally, it contains the `routes` function, which customizes routes for our application.

In this example, the following endpoints are implemented:

-   GET `/data-application/addresses`: Lists all devices within the state.
-   GET `/data-application/addresses/${address}`: Displays information about the device associated with the provided address, or indicates "Not Found".
-   GET `/data-application/addresses/${address}/transactions`: Lists all transactions for a device up to the current snapshot. This method utilizes chaining. In other words, it retrieves the lastSnapshotOrdinal of each snapshot associated with the provided address having transactions, and then acquires the transactions of this snapshot.

The remaining methods are implemented in the Data class (shared_data).

## Scripts

This example encompasses a script for generating, signing, and transmitting data updates to the metagraph in `scripts/send_data_transaction.js`. This straightforward script requires the `globalL0Url` and `metagraphL1DataUrl` to match your metagraph's configuration. Additionally, you'll need to provide a private key representing the device (client) sending the transaction. This key is used for signing the transaction and logging into your network wallet.

### Usage

1.  Assuming you have Node.js installed, navigate to the directory and execute: `npm i`.
2.  Substitute your own values for the `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` variables.
3.  Run the script using `node send_data_transaction.js`.
4.  To observe the updated state after each update, query the state GET endpoint at `<your L1 base url>/data-application/addresses`.
