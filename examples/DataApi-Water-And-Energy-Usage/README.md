# Metagraph - Data API - Water and Energy Usage

This example uses the Data API to demonstrate a basic IoT use case. In the example, a client, representing a sensor, sends signed data updates to a metagraph which validates the data before merging the updates into snapshot state.

## Template
Primary code for the example can be found in the following files:

`modules/l0/src/main/scala/com/my/currency/l0/Main.scala`

`modules/l1/src/main/scala/com/my/currency/l1/Main.scala`

`modules/data_l1/src/main/scala/com/my/currency/data_l1/Main.scala`

`modules/shared_data/src/main/scala/com/my/currency/shared_data/Main.scala`

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

This method performs initial validation on the update on the L1 layer. It does not have access to context information (state) so is limited in the kinds of validation that it can perform. If an error is returned from this method, it will be returned as a 500 response from the `/data` POST endpoint. 

-> `validateData`

This method performs validation on the data on the L0 layer and has access to context information including the current state. For this example, we check that the provided address is the same as the address that signed the message. We also check the most recent update timestamp in order to prevent old data or duplicated data from being accepted.

-> `combine`

This method accepts validated data and the previous state which is combined to return the new state. For this example, we update device information in state based on the validated update. 

-> `serializeState`

This method accepts the State object and serializes it to a byte array for storage in the snapshot.

-> `serializeUpdate`

This method accepts the Update object and serializes it to a byte array. Updates are serialized before their input signatures are checked for validity.


-> `deserializeState` and `deserializeUpdate`

The opposite of `serializeState` and `serializeUpdate`, this will deserialize to State and Update objects, respectively.

### l0

The L0 module contains the `genesis` function. This function will initialize our state, in this case, we initialize with an empty `devices` map. The remaining methods are implemented in the Data class (shared_data). 

### l1

The currency L1 layer, in this example we use a default implementation.

### data_l1

The module that will receive the requests to be processed. This module contains the `/data` POST endpoint which accepts incoming data requests. 
This module also contains the `routes` function. This function will implement custom routes to our application.

In this example, we will implement 2 different endpoints:
  -> The first one will list all the devices of the state
  -> The second one will show the information of the device of the provided address, or Not Found

The remaining methods are implemented in the Data class (shared_data). 

## Scripts

This example includes a script to generate, sign, and send data updates to the metagraph in `scripts/send_data_transaction.js`. This is a simple script where you must provide the `globalL0Url` and the `metagraphL1DataUrl` to match the configuration of your metagraph. You also must provide a private key representing the device (client) that is sending the transaction, this key will be used to sign the transaction and to log in your wallet to the network.

### Usage
- With node installed, move to the directory and then type: `npm i`.
- Replace the `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` variables with your values. 
- Run the script with `node send_data_transaction.js`  
- Query the state GET endpoint at `<your L1 base url>/data-application/addresses` to see the updated state after each update. 
