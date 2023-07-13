# Metagraph - Data API - Water and Energy Usage

This example uses the Data API to demonstrate a basic IoT use case. In the example a client, representing a sensor, sends signed data updates to a metagraph which validates the data before merging the updates into snapshot state.

## Template

This project contains dependencies of the Tessellation on version `develop`

At this example you can take a look at the files:

`modules/l0/src/main/scala/com/my/currency/l0/Main.scala`

`modules/l1/src/main/scala/com/my/currency/l1/Main.scala`

`modules/data_l1/src/main/scala/com/my/currency/data_l1/Main.scala`

`modules/shared_data/src/main/scala/com/my/currency/shared_data/Main.scala`

### shared_data

-> `validateUpdate`

- This method performs initial validation on the update on the L1 layer. It does not have access to state information. 

-> `validateData`

- This function will check the provided data to verify its validity. This validation happens on the L0 layer. In this case, we consider the `oldState` and the `proofs`. For this example, we check that the provided address is the same as the address that signed the message. We also check the most recent update timestamp in order to prevent old data or duplicated data from being accepted.

-> `combine`

- This method accepts validated data and the previous state which is combined to return the new state.

-> `serializeState`

- This is the functions that will serialize the state JSON. This will take the State object and transform it into a JSON. The JSON should be like this:

```

{ "devices" : { "DAG8py4LY1sr8ZZM3aryeP85NuhgsCYcPKuhhbw6": { "waterUsage": { "usage": 10, "timestamp": 10 }, "energyUsage": { "usage": 100, "timestamp": 21 } } } }

```

-> `serializeUpdate`

- This is the functions that will serialize the update JSON. This will take the Update object and transform it into a JSON. The JSON should be like this:

```

{"address":"DAG15uzQZ3LLKXMcpqBwtBEp2EzdgUVzpf9nQXAF","energyUsage":{"usage":7,"timestamp":1689190483073},"waterUsage":{"usage":7,"timestamp":1689190483073}}

```

-> `deserializeState` and `deserializeUpdate`

- The opposite of `serializeState` and `serializeUpdate`, this will deserialize the JSON.

-> `dataEncoder` and `dataDecoder`

- The encoder and decoder

### l0

The L0 module contains the `genesis` function. This function will initialize our state, in this case, we initialize with empty devices.
The remaining functions were explained above on the `shared_data` module

### l1

The currency L1 layer, in this example we don't use a custom implementation

### data_l1

The module that will receive the requests to be processed. This module contains the `/data` endpoint, where we gonna send the requests.

The functions were explained above on the `shared_data` module

## Scripts

We create a script to send some data transactions to this example. This is a simple script where you must provide the `globalL0Url` and the `metagraphL1DataUrl`. You should also provide one private key to send the transaction, this key will be used to sign the transaction and to log in your wallet to the network.

The script was made in NodeJS 16.4.0.

With node installed, move to the directory and then type: `npm i`

Then you can send some transactions to your metagraph (after replacing the URLs and the private key)

To run the script type: `node send_data_transaction.js`
