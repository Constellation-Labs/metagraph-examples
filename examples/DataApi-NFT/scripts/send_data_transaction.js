const { dag4 } = require("@stardust-collective/dag4");
const jsSha256 = require("js-sha256");
const axios = require("axios");

const buildMintCollection = () => {
  return {
    MintCollection: {
      name: ":collectionName",
    },
  };
};

const buildMintNFT = () => {
  return {
    MintNFT: {
      owner: ":ownerAddress",
      collectionId: ":collectionId",
      nftId: ":nftIdAsNumber",
      uri: ":validNFTURL",
      name: ":nftName",
      description: ":nftDescription",
      metadata: {
        key: "value",
      },
    },
  };
};

const buildTransferCollection = () => {
  return {
    TransferCollection: {
      fromAddress: ":fromAddress",
      toAddress: ":toAddress",
      collectionId: ":collectionId",
    },
  };
};

const buildTransferNFT = () => {
  return {
    TransferNFT: {
      fromAddress: ":fromAddress",
      toAddress: ":toAddress",
      collectionId: ":collectionId",
      nftId: ":nftIdAsNumber",
    },
  };
};

/** Encode message according with serializeUpdate on your template module l1 */
const getEncoded = (value) => {
  const energyValue = JSON.stringify(value);
  return energyValue;
};

const serialize = (msg) => {
  const coded = Buffer.from(msg, "utf8").toString("hex");
  return coded;
};

const generateProof = async (message, walletPrivateKey, account) => {
  const encoded = getEncoded(message);
  console.log(encoded);

  const serializedTx = serialize(encoded);
  const hash = jsSha256.sha256(Buffer.from(serializedTx, "hex"));
  const signature = await dag4.keyStore.sign(walletPrivateKey, hash);

  const publicKey = account.publicKey;
  const uncompressedPublicKey =
    publicKey.length === 128 ? "04" + publicKey : publicKey;

  return {
    id: uncompressedPublicKey.substring(2),
    signature,
  };
};

const sendDataTransactionsUsingUrls = async (
  globalL0Url,
  metagraphL1DataUrl
) => {
  const walletPrivateKey = ":private_key";

  const account = dag4.createAccount();
  account.loginPrivateKey(walletPrivateKey);

  account.connect({
    networkVersion: "2.0",
    l0Url: globalL0Url,
    testnet: true,
  });

  const message = buildMintCollection();
  const proof = await generateProof(message, walletPrivateKey, account);
  const body = {
    value: {
      ...message,
    },
    proofs: [proof],
  };
  try {
    console.log(`Transaction body: ${JSON.stringify(body)}`);
    const response = await axios.post(`${metagraphL1DataUrl}/data`, body);
    console.log(`Response: ${JSON.stringify(response.data)}`);
  } catch (e) {
    console.log("Error sending transaction", e.message);
  }
  return;
};

const sendDataTransaction = async () => {
  const globalL0Url = ":your_global_l0_node_url";
  const metagraphL1DataUrl = ":your_metagraph_l1_data_url";

  await sendDataTransactionsUsingUrls(globalL0Url, metagraphL1DataUrl);
};

sendDataTransaction();
