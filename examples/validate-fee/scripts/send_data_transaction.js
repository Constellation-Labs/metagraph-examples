const { dag4 } = require( '@stardust-collective/dag4' );
const jsSha256 = require( 'js-sha256' );
const axios = require( 'axios' );
const path = require('node:path');

/****************************************************/
/** Fill in these constants with appropriate values. */

const globalL0Url = ':your_global_l0_node_url';
const metagraphL1DataUrl = ':your_metagraph_l1_data_url';
const walletPrivateKey = ':private_key';

/** Object name of DataUpdate messages to send; must names of data types defined in metagraph. */
const typeOne = "UpdateTypeOne";
const typeTwo = "UpdateTypeTwo";

/****************************************************/

const usage = (msg) => {
    if (msg) console.log(msg);
    console.log(`Usage: node  ${path.basename(process.argv[1])}  ${typeOne}|${typeTwo}  [<ordinal> <hash>]`);
    console.log(' where ordinal and hash are the values to set for the parent portion of the message.');
    console.log(' The ordinal must be greater than 0 and the hash must be the value returned for the previous update.');
    console.log(' If no ordinal and hash are provided then the parent ordinal is set to zero.');
    process.exit(1);
}

const updateTypeName = ( updateType ) => {
    switch ( updateType  ) {
        case typeOne: return "update type one";
        case typeTwo: return "update type two";
        default: return "unknown type";
    }
};

const updateTypeDestination = ( updateType ) => {
    switch ( updateType  ) {
        case typeOne: return 'DAG4o41NzhfX6DyYBTTXu6sJa6awm36abJpv89jB';
        case typeTwo: return 'DAG4Zd2W2JxL1f1gsHQCoaKrRonPSSHLgcqD7osU';
        default: return "unknown address";
    }
};

/** Construct message.
  * This message is sent to the /data/estimate-fee endpoint.
  * The amount will then be updated from the response before sending to the /data endpoint.
  * The parent is "empty", as if this is the very first message;
  * subsequent messages should provide appropriate parent fields.
  */
const buildMessage = ( type ) => {
    var msg = {};
    msg[type] = {
        name: `${updateTypeName( type )}`,
        fee: {
            source: 'DAG8pkb7EhCkT3yU87B2yPBunSCPnEdmX2Wv24sZ',
            destination: `${updateTypeDestination( type )}`,
            amount: 0,
            parent: {
                ordinal: parentOrdinal,
                hash: `${parentHash}`
            },
            salt: 0
        }
    };
    return msg;
};

/** Encode message according with serializeUpdate on your template module l1 */
const getEncoded = ( value ) => JSON.stringify( value );

const serialize = ( msg ) => Buffer.from( msg, 'utf8' );

/** https://codereview.stackexchange.com/questions/265820/using-javascript-given-a-json-value-recursively-find-all-json-objects-then-so
 * The validate-fee application currently does not require the JSON to be sorted; use this if that changes.
 */
const sortJson = ( o ) => {
  const isObj = x => typeof(x) === 'object'
  const isArr = Array.isArray

  // Beware: null is type object, but is not sortable.
  const isSortable = x => (x !== null) && (isObj(x) || isArr(x))

  if (!isSortable(o))
    return o

  if (isArr(o))
    return o.map(sortJson)

  if (isObj(o))
    return Object.keys(o).sort().reduce(
      (m, x) => (m[x] = isSortable(o[x]) ? sortJson(o[x]) : o[x], m),
      {}
    )
}

// Our JSON does not contain nulls but if yours does you'll need to remove them.
const hashJson = ( value ) =>
  jsSha256.sha256(
    serialize(
      getEncoded(
        value // sortJson(value) if application needs it
      )
    )
  );

const generateProof = async ( message, walletPrivateKey, account ) => {
    const encodedMessage = Buffer.from(JSON.stringify(message)).toString('base64');
    const signature = await dag4.keyStore.dataSign( walletPrivateKey, encodedMessage );

    const publicKey = account.publicKey;
    const uncompressedPublicKey = publicKey.length === 128 ? '04' + publicKey : publicKey;

    return {
        id: uncompressedPublicKey.substring( 2 ),
        signature
    };
};

const sendDataTransactionsUsingUrls = async (
    globalL0Url,
    metagraphL1DataUrl
) => {
    const account = dag4.createAccount();
    account.loginPrivateKey( walletPrivateKey );

    account.connect( {
        networkVersion: '2.0',
        l0Url: globalL0Url,
        testnet: true
    } );

    const message = buildMessage( updateType );

    const dataUrl = `${metagraphL1DataUrl}/data`;
    try {
        //console.log( `Estimate Fee body: ${JSON.stringify( message, null, 2 )}` );
        const estimateResponse = await axios.post( `${dataUrl}/estimate-fee`, message );
        //console.log( `Estimate Fee JSON: ${JSON.stringify( estimateResponse.data )}` );

        message[updateType].fee.amount = estimateResponse.data.fee;

        const feeTxn = message[updateType].fee;
        const feeTxnHash = hashJson(feeTxn);
        //console.log(`FeeTransaction: ${JSON.stringify(feeTxn, null, 2)}`);

        console.log('!'.repeat(70));
        console.log('!! FeeTransaction Hash, the parent hash for the next transaction');
        console.log(`!! ${feeTxnHash}`);
        console.log('!'.repeat(70));

        const proof = await generateProof( message, walletPrivateKey, account );
        const signedMessage = {
            value: {
                ...message
            },
            proofs: [
                proof
            ]
        };

        console.log( `Transaction body: ${JSON.stringify( signedMessage, null, 2 )}` );
        const response = await axios.post( dataUrl, signedMessage );
        console.log( `Transaction Hash: ${JSON.stringify( response.data )}` );
    } catch( e ) {
        console.log( `Error sending transaction: ${e.message}`, e.response.data );
    }
    return;
};

const sendDataTransaction = async () => await sendDataTransactionsUsingUrls( globalL0Url, metagraphL1DataUrl );

var updateType;
var parentOrdinal;
var parentHash;

const emptyHash = "0".repeat(64);
if (process.argv.length === 2) {
    usage();
}
updateType = process.argv[2];
if (updateType !== typeOne && updateType !== typeTwo) {
    usage(`Unrecognized message type [${updateType}]`);
}
if (process.argv.length === 4) {
    usage('Missing parent hash');
}
if (process.argv.length === 3) {
    parentOrdinal = 0;
    parentHash = emptyHash;
}
else {
    parentOrdinal = Number(process.argv[3]);
    parentHash = process.argv[4];
    if (parentOrdinal <= 0) {
        usage(`Parent ordinal [${parentOrdinal}] is not greater than zero.`);
    }
}
sendDataTransaction();
