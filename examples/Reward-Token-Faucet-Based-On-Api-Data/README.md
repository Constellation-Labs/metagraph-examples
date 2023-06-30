# Metagraph - Token Faucet Based on API Data

This example will distribute rewards to addresses returned from a custom API endpoint.
We will use a fixed amount of tokens to be distributed, in this case, 100 units of the token, but it's updatable.

## API

The API folder contains one simple API developed in NodeJS (Typescript) with Express. We didn't use databases in this example, so the values are stored in memory
Endpoints:
* GET /addresses -> This will return the latest 20 stored addresses.
	* Response
		* 200 
```
[
{
	"address": "DAG.....",
	"date": "2023-06-01 01:00:00"
},
{
	"address": "DAG.....",
	"date": "2023-06-01 00:40:00"
}
]
```

  * POST /addresses -> Used to store new addresses
	  * Body
  ```
  {
	  "address": "DAG..."
  }
```
* Response
	* 201 -> Created
	* 400 -> Invalid DAG wallet

### Running API
Install the dependencies: `npm i`
Run the API: `npm run dev`

You can check the API by calling the GET endpoint: `http://localhost:8000/addresses`

## Template
This project contains dependencies of the Tessellation on version `v2.0.0-alpha.7`

At this example you can take a look at the file:
`modules/l0/src/main/scala/com/my/currency/l0/Main.scala`

This file contains the logic of fetching the data on the API and then setting those addresses to be rewarded.

We will fetch from `GET /addresses` endpoint and then distribute the reward for those addresses.

We set initially the amount of 100 tokens to be distributed, so if we have 2 wallets: 100 / 2 = 50 tokens to each wallet.

If not wallet has been found on the API response, no rewards will be distributed.
  
