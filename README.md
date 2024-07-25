# Metagraph Examples

Welcome to the Metagraph Examples repository! This repository contains a collection of example codebases located in the `examples` directory. Each codebase serves as a starting point for developers to create their own projects, while also demonstrating specific functionalities of the metagraph framework. These examples are designed to help you understand and utilize various features of the Euclid SDK.

## Overview

Each example in this repository is designed to run in the Euclid SDK. The Euclid SDK is a comprehensive development environment provided by Constellation Network for building and deploying metagraphs. 

- **Euclid SDK Repo:** [Euclid Development Environment](https://github.com/Constellation-Labs/euclid-development-environment)
- **Euclid SDK Documentation:** [Euclid SDK Docs](https://docs.constellationnetwork.io/sdk/)

Each of the example repos has their own README.md file inside their directory with additional details. Most examples include scripts to demonstrate how to send data to the metagraph endpoints and several of the examples (Voting, NFT) also include a React JS frontend that can be used to interact with the metagraph. 

## Installation

You can install any of the example codebases using Euclid's CLI tool, `hydra`. 

To install a specific example, use the following command:

```sh
hydra install-template <example-repo>
```

For a full list of available examples, run:
```sh
hydra install-template --list
```

## Examples
Below is a list of available example codebases and the specific functionalities they demonstrate:

### Currency: Transaction Validation
**Description:** A currency application that demonstrates the basics of custom transaction validation. 
**Functionality:** Basic currency app, custom transaction validation.

### Currency: Reward API
**Description:** A currency application that mints tokens based on the results of an external API call. 
**Functionality:** Reward distribution, external API integrations. 

### Data: Custom NFT Implementation
**Description:** This metagraph codebase implements custom NFTs following the ERC-721 standard.
**Functionality:** Custom data types, data validation, frontend UI.

### Data: Voting Poll
**Description:** A data application that implements polls with token-based voting, displaying a basic DAO use case.
**Functionality:** Custom data types, data validation, frontend UI, Stargazer wallet integration. 

### Data: Water and Energy IoT
**Description:** A data application demonstrating a basic IoT integration use case. 
**Functionality:** Custom data types, calculated state.

## License
This repository is licensed under the Apache License 2.0. See the LICENSE file for more information.

## Contact
For questions or support, please reach out to the Constellation Network community on [Discord](https://discord.gg/hWEugRNYWt) or open an issue in this repository.
