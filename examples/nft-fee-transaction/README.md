# Example NFT Metagraph using the Data API and Fee Transactions

This example builds on the [NFT Metagraph example](https://github.com/Constellation-Labs/metagraph-examples/tree/main/examples/nft). To fully understand the flow and mechanics, please refer to the README in the original [NFT Metagraph example](https://github.com/Constellation-Labs/metagraph-examples/tree/main/examples/nft).

## Fee Transactions

This example introduces the new FeeTransaction feature, allowing you to assign fees per transaction type. It adds two new lifecycle functions: estimateFee and validateFee. These functions are optional but necessary if you want to enable fee transactions in your metagraph.

### Estimating Fees

The estimateFee endpoint allows users to check the required fees before sending an update.
For example, when minting a new collection, you can send a `POST` request to `/data/estimate-fee` with the following body:
```json
{
    "MintCollection": {
        "name": "MyCollection"
    }
}
```
The response will look something like this:
```json
{
    "fee": 10000,
    "address": "DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ",
    "updateHash": "2782eace743eadafcb36e9c3aadab598dd5aae58de50a81f5b193c517acca763"
}
```
Here's what each field represents:

-  **fee**: The fee amount required for the transaction.

-  **address**: The destination address where the fees should be sent.

-  **updateHash**: The hash of the update, which you need to reference in the dataUpdateRef field of the fee transaction (explained in the next section).

The response is generated using this function as base:
```scala
override def estimateFee(gsOrdinal: SnapshotOrdinal)(update: NFTUpdate)(implicit context: L1NodeContext[IO], A: Applicative[IO]): IO[EstimatedFee] = {
  update match {
    case _: MintCollection => IO.pure(EstimatedFee(Amount(NonNegLong(10000)), Address("DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ")))
    case _: MintNFT => IO.pure(EstimatedFee(Amount(NonNegLong(110000)), Address("DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ")))
    case _: TransferCollection => IO.pure(EstimatedFee(Amount(NonNegLong(120000)), Address("DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ")))
    case _: TransferNFT => IO.pure(EstimatedFee(Amount(NonNegLong(130000)), Address("DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ")))
  }
}
```

### Fee Validation

The validateFee function checks if a fee transaction is provided for the data update. You can decide if a fee transaction is required for each update type. Here’s how it works:
```scala
override def validateFee(
  gsOrdinal: SnapshotOrdinal
)(dataUpdate: Signed[NFTUpdate], maybeFeeTransaction: Option[Signed[FeeTransaction]])(
  implicit context: L0NodeContext[IO], A: Applicative[IO]
): IO[DataApplicationValidationErrorOr[Unit]] = {
  maybeFeeTransaction match {
    case Some(feeTransaction) =>
      dataUpdate.value match {
        case _: MintCollection =>
          if (feeTransaction.value.amount.value.value < 10000)
            NotEnoughFee.invalidNec[Unit].pure[IO]
          else
            ().validNec[DataApplicationValidationError].pure[IO]
        case _ =>
          ().validNec[DataApplicationValidationError].pure[IO]
      }
    case None =>
      MissingFeeTransaction.invalidNec[Unit].pure[IO]
  }
}
```

In this function:

-  For the MintCollection update, if the fee is less than 10000 tokens, the transaction will be rejected.

-  For other updates, such as MintNFT, TransferNFT, and TransferCollection, fees are optional, so the transaction may be accepted without a fee transaction.

This ensures flexible fee validation based on the type of update being processed.

### Sending a Fee Transaction

After estimating the fee, you can submit the update along with the corresponding fee transaction. Here’s an example of the body to send:
```json
{
    "data": {
        "value": {
            "MintCollection": {
                "name": "MyCollection"
            }
        },
        "proofs": [
            {
                "id": "db2faf200159ca3c47924bf5f3bda4f45d681a39f9490053ecf98d788122f7a7973693570bd242e10ab670748e86139847eb682a53c7c5c711b832517ce34860",
                "signature": ":data_signature"
            }
        ]
    },
    "fee": {
        "value": {
            "source": "DAG6t89ps7G8bfS2WuTcNUAy9Pg8xWqiEHjrrLAZ",
            "destination": "DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ",
            "amount": 10000,
            "dataUpdateRef": "2782eace743eadafcb36e9c3aadab598dd5aae58de50a81f5b193c517acca763"
        },
        "proofs": [
            {
                "id": "db2faf200159ca3c47924bf5f3bda4f45d681a39f9490053ecf98d788122f7a7973693570bd242e10ab670748e86139847eb682a53c7c5c711b832517ce34860",
                "signature": ":fee_transaction_signature"
            }
        ]
    }
}
```
In this JSON:

-  **destination**, **amount**, and **dataUpdateRef** are filled in with values returned from the estimateFee endpoint.

-  The **source** field must match the wallet that signed the transaction; otherwise, validation will fail.

This schema introduces fee transactions, but if you don't want to use them, you can simply send the data part of the schema. Fee transactions are optional, and backward compatibility is maintained with older schemas.

A successful response will look like this:
```json
{
    "feeHash": "1cfe66f2590e1838ae3ae59ed2f22465b1b36839707b177392be61efe6f2e682",
    "hash": "2782eace743eadafcb36e9c3aadab598dd5aae58de50a81f5b193c517acca763"
}
```
In this example, we’re minting a new collection called `MyCollection` and paying a fee of `10000` tokens to the destination address `DAG88C9WDSKH451sisyEP3hAkgCKn5DN72fuwjfQ`.

You can find the scripts directory where you can send the transactions with fees to your metagraph. Please take a look at the file:
`scripts/send_data_transaction.js`