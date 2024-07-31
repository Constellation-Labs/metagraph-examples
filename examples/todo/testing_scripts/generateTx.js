const { dag4 } = require("@stardust-collective/dag4");
const jsSha256 = require("js-sha256");
const axios = require("axios");

// Command line arguments
const jsonFilePath = process.argv[2];

const buildTx = require(jsonFilePath);

const generateProof = async (message, walletPrivateKey, account) => {

  // Sort object by keys
  const sortObjectByKey = (sourceObject) => {
    if (Array.isArray(sourceObject)) {
      return sourceObject.map(sortObjectByKey);
    } else if (typeof sourceObject === 'object' && sourceObject !== null) {
      return Object.keys(sourceObject).sort().reduce((sortedObj, key) => {
        const value = sourceObject[key];
        sortedObj[key] = sortObjectByKey(value);
        return sortedObj;
      }, {});
    } else {
      return sourceObject;
    }
  }

  // Recursively remove nulls
  const removeNulls = (obj) => {
    if (Array.isArray(obj)) {
      return obj.filter(v => v !== null).map(v => v && typeof v === "object" ? removeNulls(v) : v)
    } else if (typeof obj === 'object' && obj !== null) {
      return Object.fromEntries(
        Object.entries(obj)
          .filter(([, v]) => v !== null)
          .map(([k, v]) => [k, v && typeof v === "object" ? removeNulls(v) : v])
      );
    } else {
      return obj;
    }
  }

  const getEncoded = (value) => {
    let nonNullValue = removeNulls(value);
    let sortedValue = sortObjectByKey(nonNullValue);
    return JSON.stringify(sortedValue);
  };

  const serialize = (data) => {
    return Buffer.from(data, 'utf8');
  }


  const encoded = getEncoded(message)
  const serialized = serialize(encoded)
  const hash = jsSha256.sha256(serialized)

  console.log(`\nMessage (JSON): ${encoded}`)
  console.log(`\nHash: ${hash}`)

  const signature = await dag4.keyStore.sign(walletPrivateKey, hash)

  const publicKey = account.publicKey
  const uncompressedPublicKey = publicKey.length === 128 ? '04' + publicKey : publicKey

  return {
    id: uncompressedPublicKey.substring(2),
    signature,
  }
}

const sendDataTransactionsUsingUrls = async (
  globalL0Url,
  metagraphL1DataUrl
) => {
  const generateWallet = () => {
    const privateKey = dag4.keyStore.generatePrivateKey();
    const account = dag4.createAccount();
    account.loginPrivateKey(privateKey);
    account.connect({
      networkVersion: "2.0",
      l0Url: globalL0Url,
      testnet: true,
    });

    // console.log(`{"address": "${account.address}", "privateKey": "${privateKey}" }`);
    return { account, privateKey };
  };

  const { account, privateKey } = generateWallet();

  const message = buildTx();
  const proof = await generateProof(message, privateKey, account);
  const body = {
    value: {
      ...message,
    },
    proofs: [proof],
  };
  try {
    console.log(`\nTransaction body: ${JSON.stringify(body)}`);
    const response = await axios.post(`${metagraphL1DataUrl}/data`, body);
    console.log(`\nResponse: ${JSON.stringify(response.data)}`);
  } catch (e) {
    console.log("\nError sending transaction", e.response ? e.response.data : e.message);
  }
  return;
};

const sendDataTransaction = async () => {
  const globalL0Url = "http://127.0.0.1:9000";
  const metagraphL1DataUrl = "http://127.0.0.1:9400";

  await sendDataTransactionsUsingUrls(globalL0Url, metagraphL1DataUrl);
};

sendDataTransaction();
