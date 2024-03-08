const { dag4 } = require("@stardust-collective/dag4");
const jsSha256 = require("js-sha256");
const jsSha512 = require("js-sha512");
const EC = require("elliptic");
const axios = require("axios");

const curve = new EC.ec("secp256k1");

/** Encode message according with serializeUpdate on your template module l1 */
const getEncoded = (value) => {
  const energyValue = JSON.stringify(value);
  return energyValue;
};

const serialize = (msg) => {
  const coded = Buffer.from(msg, "utf8").toString("hex");
  return coded;
};

const sha256 = (hash) => {
  return jsSha256.sha256(hash);
};

const sha512 = (hash) => {
  return jsSha512.sha512(hash);
};

const sign = async (privateKey, msg) => {
  const sha512Hash = sha512(msg);

  const ecSig = curve.sign(sha512Hash, Buffer.from(privateKey, "hex")); //, {canonical: true});
  return Buffer.from(ecSig.toDER()).toString("hex");
};

const sendDataTransactionsUsingUrls = async (
  globalL0Url,
  metagraphL1DataUrl
) => {
  const walletPrivateKey = ":device_private_key";

  const account1 = dag4.createAccount();
  account1.loginPrivateKey(walletPrivateKey);

  account1.connect({
    networkVersion: "2.0",
    l0Url: globalL0Url,
    testnet: true,
  });

  const message = {
    address: ":device_address",
    energyUsage: {
      usage: 7,
      timestamp: new Date().getTime(),
    },
    waterUsage: {
      usage: 7,
      timestamp: new Date().getTime(),
    },
  };

  const encoded = getEncoded(message);
  console.log(encoded);
  const serializedTx = serialize(encoded);
  const hash = sha256(Buffer.from(serializedTx, "hex"));
  console.log(hash);

  const signature = await sign(walletPrivateKey, hash);

  const publicKey = account1.publicKey;
  const uncompressedPublicKey =
    publicKey.length === 128 ? "04" + publicKey : publicKey;

  const body = {
    value: {
      ...message,
    },
    proofs: [
      {
        id: uncompressedPublicKey.substring(2),
        signature,
      },
    ],
  };
  try {
    console.log(`Transaction body: ${JSON.stringify(body)}`);
    const response = await axios.post(`${metagraphL1DataUrl}/data`, body);
    console.log(`Response: ${JSON.stringify(response.data)}`);
  } catch (e) {
    console.log("Error sending transaction", e);
  }
  return;
};

const sendDataTransaction = async () => {
  const globalL0Url = ":global_l0_url";
  const metagraphL1DataUrl = ":metagraph_l1_data_url";

  await sendDataTransactionsUsingUrls(globalL0Url, metagraphL1DataUrl);
};

sendDataTransaction();
