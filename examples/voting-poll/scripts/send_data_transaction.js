const { dag4 } = require( '@stardust-collective/dag4' );
const jsSha256 = require( 'js-sha256' );
const axios = require( 'axios' );

const buildCreatePollMessage = () => {
    return {
        CreatePoll: {
            name: ':poll_name',
            owner: ':your_address',
            pollOptions: [ ':option_1', ':option_2' ],
            startSnapshotOrdinal: 1, //start_snapshot, you should replace
            endSnapshotOrdinal: 100 //end_snapshot, you should replace
        }
    };
};

const buildCreateVoteMessage = () => {
    return {
        VoteInPoll: {
            pollId: ':pool_id',
            address: ':your_address',
            option: ':option'
        }
    };
};

/** Encode message according with serializeUpdate on your template module l1 */
const getEncoded = ( value ) => {
    const energyValue = JSON.stringify( value );
    return energyValue;
};

const serialize = ( msg ) => {
    const coded = Buffer.from( msg, 'utf8' ).toString( 'hex' );
    return coded;
};

const generateProof = async ( message, walletPrivateKey, account ) => {
    const encodedMessage = Buffer.from(JSON.stringify(message)).toString('base64')
    const signature = await dag4.keyStore.dataSign( walletPrivateKey, encodedMessage );

    const publicKey = account.publicKey;
    const uncompressedPublicKey =
    publicKey.length === 128 ? '04' + publicKey : publicKey;

    return {
        id: uncompressedPublicKey.substring( 2 ),
        signature
    };
};

const sendDataTransactionsUsingUrls = async (
    globalL0Url,
    metagraphL1DataUrl
) => {
    const walletPrivateKey = ':private_key';

    const account = dag4.createAccount();
    account.loginPrivateKey( walletPrivateKey );

    account.connect( {
        networkVersion: '2.0',
        l0Url: globalL0Url,
        testnet: true
    } );

    const message = buildCreateVoteMessage();
    const proof = await generateProof( message, walletPrivateKey, account );
    const body = {
        value: {
            ...message
        },
        proofs: [
            proof
        ]
    };
    try {
        console.log( `Transaction body: ${JSON.stringify( body )}` );
        const response = await axios.post( `${metagraphL1DataUrl}/data`, body );
        console.log( `Response: ${JSON.stringify( response.data )}` );
    } catch( e ) {
        console.log( 'Error sending transaction', e.message );
    }
    return;
};

const sendDataTransaction = async () => {
    const globalL0Url = ':your_global_l0_node_url';
    const metagraphL1DataUrl = ':your_metagraph_l1_data_url';

    await sendDataTransactionsUsingUrls( globalL0Url, metagraphL1DataUrl );
};

sendDataTransaction();
