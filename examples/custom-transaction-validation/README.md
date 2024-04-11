# Metagraph - Custom transaction validation

This example provides a sample implementation of a currency metagraph with a custom transaction validator. In this case, the validator prevents transactions below a minimum fee from being accepted.

The CurrencyL1App provides `transactionValidator` as an overridable method to introduce custom validation logic to token transactions. Throwing an error from this method will reject the transaction and prevent its execution. 

The following validation criteria are implemented in this example:
- If snapshot ordinal < 10 then pass
- If snapshot ordinal >= 10 then pass only if fee is greater than 10

## Template
This project contains dependencies of the Tessellation on version `v2.6.0`

The primary implementation of this example is found in: 
`modules/l1/src/main/scala/com/my/custom_validator/l1/Main.scala`

This file contains the sample implementation of custom validator.
  
