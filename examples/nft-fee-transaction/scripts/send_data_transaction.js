const { dag4 } = require("@stardust-collective/dag4");
const jsSha256 = require("js-sha256");
const axios = require("axios");

const buildMintCollection = () => {
    return {
        MintCollection: {
            name: "MyCollection",
        },
    };
};

const buildMintNFT = () => {
    return {
        MintNFT: {
            owner: "",
            collectionId: "",
            nftId: 1,
            uri: "",
            name: "",
            description: "",
            metadata: {
                key: "",
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

const buildFeeTransaction = () => {
    return {
        source: ":source_wallet",
        destination: ":destination_wallet",
        amount: 10000,
        dataUpdateRef: ":data_update_ref"
    }
}

/** Encode message according with serializeUpdate on your template module l1 */
const getEncoded = (value) => {
    const energyValue = JSON.stringify(value);
    return energyValue;
};

const serialize = (msg) => {
    const coded = Buffer.from(msg, 'utf8').toString('hex');
    return coded;
};

const generateProofFee = async (message, walletPrivateKey, account) => {
    const encoded = getEncoded(message);
    console.log(encoded);

    const serializedTx = serialize(encoded);
    const hash = jsSha256.sha256(Buffer.from(serializedTx, 'hex'));
    const signature = await dag4.keyStore.sign(walletPrivateKey, hash);

    const publicKey = account.publicKey;
    const uncompressedPublicKey =
        publicKey.length === 128 ? '04' + publicKey : publicKey;

    return {
        id: uncompressedPublicKey.substring(2),
        signature
    };
};

const generateProof = async (message, walletPrivateKey, account) => {
    const encodedMessage = Buffer.from(JSON.stringify(message)).toString('base64')
    const signature = await dag4.keyStore.dataSign(
        walletPrivateKey,
        encodedMessage
    );

    const publicKey = account.publicKey;
    const uncompressedPublicKey =
        publicKey.length === 128 ? '04' + publicKey : publicKey;

    return {
        id: uncompressedPublicKey.substring(2),
        signature
    };
};

const sendDataTransactionsUsingUrls = async (
    globalL0Url,
    metagraphL1DataUrl
) => {
    const walletPrivateKey = ":wallet_private_key";

    const account = dag4.createAccount();
    account.loginPrivateKey(walletPrivateKey);

    account.connect({
        networkVersion: "2.0",
        l0Url: globalL0Url,
        testnet: true,
    });
    const message = buildMintCollection()
    const fee = buildFeeTransaction()

    const proof = await generateProof(message, walletPrivateKey, account);
    const feeProof = await generateProofFee(fee, walletPrivateKey, account);

    const body = {
        data: {
            value: {
                ...message
            },
            proofs: [
                proof
            ]
        },
        fee: {
            value: {
                ...fee
            },
            proofs: [
                feeProof
            ]
        }
    };
    try {
        console.log(`Transaction body: ${JSON.stringify(body)}`);
        const response = await axios.post(`${metagraphL1DataUrl}/data`, body);
        console.log(`Response: ${JSON.stringify(response.data)}`);
    } catch (e) {
        console.log("Error sending transaction", JSON.stringify(e));
    }
    return;
};

const sendDataTransaction = async () => {
    const globalL0Url = 'http://localhost:9000';
    const metagraphL1DataUrl = 'http://localhost:9400';

    await sendDataTransactionsUsingUrls(globalL0Url, metagraphL1DataUrl);
};

sendDataTransaction();