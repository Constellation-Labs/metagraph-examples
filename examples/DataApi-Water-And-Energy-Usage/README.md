
# Metagraph - Data API - Water and Energy Usage

  

This is one example of the Data API, where we can send transactions to update the state of Water and Energy usage
  

## Template

This project contains dependencies of the Tessellation on version `v2.0.0-alpha.7`

 
At this example you can take a look at the files:

`modules/l0/src/main/scala/com/my/currency/l0/Main.scala`
`modules/l1/src/main/scala/com/my/currency/l1/Main.scala`


### L0
The L0 module contains the `genesis` function. This function will initialize our state, in this case, we initialize with `(0,0)` (0 of Water and Energy usage)

You can ignore the other functions for now, they will be repeated on the L1 module

  ### L1
  The module that will receive the requests to be processed. Contains the following functions:
  
->   `validateData` and `validateUpdate`
* These functions will check the provided data to verify its validity. If the amount that we want to update  of water and energy usage is valid (at this example > 0), we will proceed to the state update, otherwise, we will return an error

-> `combine`
* This function will combine the old state with the new update provided. In this case, if we provide `EnergyUsage` of 10, we will sum 0 + 10

-> `serializeState` and `serializeUpdate`
* These are the functions that will serialize the JSON. In this case, one example of request that is serialized for these functions:
 ```
 {"value":{"EnergyUsage":{"value":10}},"proofs":[{"id":"e75a6011eaa38d7b0a1cb41810c655cdc89c6c5ffd207cbab9d18fd49cbf2729e262b5387a4687a23a163d14bc0dff8ef6539e2a73932e77d2de6b1895facd99","signature":"3044022060107a64dabbc9b0e2779a9fada99646798b3ebd21ecda22c7de2740f13addc30220564d8303f1a581f9a7c021252f624911b7c3bec63620a0d000b7f651753e020d"}]}
 ```
-> `deserializeState` and `deserializeUpdate`
* The opposite of `serializeState` and `serializeUpdate`, this will deserialize the JSON.

-> `dataEncoder` and `dataDecoder`
* The encoder and decoder
  
 ## Scripts
 We create a script to send some data transactions to this example. This is a simple script where you must provide the `globalL0Url` and the `metagraphL1DataUrl`. You should also provide one private key to send the transaction, this key will be used to sign the transaction and to log in your wallet to the network.

 The script was made in NodeJS 16.4.0.

With node installed, move to the directory and then type: `npm i`

Then you can send some transactions to your metagraph (after replacing the URLs and the private key)

To run the script type: `node send_data_transaction.js`