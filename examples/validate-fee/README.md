# Metagraph - Data Application - Validate Fee

This example demonstrates a implementation of the data application service call _validateFee_
and the _estimate-fee_ endpoint.

## Template

Primary code for the example can be found in the following files:

`modules/data_l1/src/main/scala/com/my/validate_fee/Main.scala`

## Scripts

`scripts/send_data_transaction.js` is a script to send a `DataUpdate` message to the metagraph.

### Usage

With node installed, type: `npm i`.

Before using the script it needs to be edited to set the three required variables:
- `:your_global_l0_node_url`
- `:your_metagraph_l1_data_url`
- `:private_key`

Alternatively, one can create a script to create a new script without altering the original.
For example, this Bash script was used during development:
```shell
$ cat run
sed \
  -e 's!:your_global_l0_node_url!http://localhost:9000!g'    \
  -e 's!:your_metagraph_l1_data_url!http://localhost:9400!g' \
  -e 's/:private_key/your key/g'  \
 < ~/dev/metagraph-examples/examples/validate-fee/scripts/send_data_transaction.js \
 > index.js

node index.js $@
```

This is the help message displayed when invoked with no arguments:

```text
Usage: node  send_data_transaction.js  UpdateTypeOne|UpdateTypeTwo  [<ordinal> <hash>]
 where ordinal and hash are the values to set for the parent portion of the message.
 The ordinal must be greater than 0 and the hash must be the value returned for the previous update.
 If no ordinal and hash are provided then the parent ordinal is set to zero.
```

The very first update should be sent without parent information, for example:
```text
$ ./run UpdateTypeOne
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!! FeeTransaction Hash, the parent hash for the next transaction
!! ad88c25333e6e75a4aab4ed0a2585cafb8e6984d9429d2a263431b1563fd2740
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
Transaction body: {
  "value": {
    "UpdateTypeOne": {
      "name": "update type one",
      "fee": {
        "source": "DAG8pkb7EhCkT3yU87B2yPBunSCPnEdmX2Wv24sZ",
        "destination": "DAG4o41NzhfX6DyYBTTXu6sJa6awm36abJpv89jB",
        "amount": 123,
        "parent": {
          "ordinal": 0,
          "hash": "0000000000000000000000000000000000000000000000000000000000000000"
        },
        "salt": 0
      }
    }
  },
  "proofs": [
    [snip]
  ]
}
```
The arguments for the next update should use the next ordinal and the provided hash:
```text
./run UpdateTypeOne 1 "ad88c25333e6e75a4aab4ed0a2585cafb8e6984d9429d2a263431b1563fd2740"
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
!! FeeTransaction Hash, the parent hash for the next transaction
!! 4a57a3abe18709904cd2fab7808bf0988505296878bc58691e5b41e6fe125676
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
Transaction body: {
  "value": {
    "UpdateTypeOne": {
      "name": "update type one",
      "fee": {
        "source": "DAG8pkb7EhCkT3yU87B2yPBunSCPnEdmX2Wv24sZ",
        "destination": "DAG4o41NzhfX6DyYBTTXu6sJa6awm36abJpv89jB",
        "amount": 123,
        "parent": {
          "ordinal": 1,
          "hash": "ad88c25333e6e75a4aab4ed0a2585cafb8e6984d9429d2a263431b1563fd2740"
        },
        "salt": 0
      }
    }
  },
  "proofs": [
    [snip]
  ]
}
```

