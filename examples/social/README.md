Metagraph - Data Application - Social
=============================

This example demonstrates a basic social media use case using the Data API. In the example, a client can send four types of signed data updates to a metagraph: one to create a post, another to edit a post, another to delete a post, and another to subscribe to a user. These updates are validated before being merged into the snapshot state.

Here's how the social media system works:

-   Each user can create, edit, and delete their own posts.
-   Each user can subscribe to other users to follow their posts.
-   Posts and subscriptions are validated to ensure their integrity and consistency.
-   Users cannot create duplicate posts with the same content.
-   This example persists the calculatedState in an external database, in this example PostgresSQL.
-   To use external storage, we've created a new service in the `Main.scala` files of both layers: l0 and data-l1. This new service implements the trait `ExternalStorageService`. Our `CalculatedStateService` receives an implementation of `ExternalStorageService` and calls the function to set the calculated state externally when we call the set function of `CalculatedStateService`. This function also was updated to work atomically.  
-   For more information, please refer to the section `Persisting User Calculated State to PostgreSQL`.
Template
--------

Primary code for the example can be found in the following files:

`modules/l0/src/main/scala/com/my/currency/l0/*`

`modules/l1/src/main/scala/com/my/currency/l1/*`

`modules/data_l1/src/main/scala/com/my/currency/data_l1/*`

`modules/shared_data/src/main/scala/com/my/currency/shared_data/*`

### Application Lifecycle

The methods of the DataApplication are invoked in the following sequence:

-   `validateUpdate`
-   `validateData`
-   `combine`
-   `dataEncoder`
-   `dataDecoder`
-   `calculatedStateEncoder`
-   `signedDataEntityDecoder`
-   `serializeBlock`
-   `deserializeBlock`
-   `serializeState`
-   `deserializeState`
-   `serializeUpdate`
-   `deserializeUpdate`
-   `setCalculatedState`
-   `getCalculatedState`
-   `hashCalculatedState`
-   `routes`

For a more detailed understanding, please refer to the [complete documentation](https://docs.constellationnetwork.io/sdk/frameworks/currency/data-api) on the Data API.

### Routes

Customizes routes for our application.

In this example, the following endpoints are implemented:

-   GET `<metagraph l0 url>/data-application/users/:user_id/posts`: Returns all user posts.
-   GET `<metagraph l0 url>/data-application/users/:user_id/subscriptions`: Returns the subscriptions of a user.
-   GET `<metagraph l0 url>/data-application/users/:user_id/feed`: Returns the user's feed of posts from their subscriptions.

Scripts
-------

This example includes a script to generate, sign, and send data updates to the metagraph in `scripts/send_data_transaction.js`. This is a simple script where you must provide the `globalL0Url` and the `metagraphL1DataUrl` to match the configuration of your metagraph. You also must provide a private key representing the user that will create, edit, delete a post, or subscribe to another user (client) that is sending the transaction. This key will be used to sign the transaction and to log in your wallet to the network.

### Usage

-   With node installed, move to the directory and then type: `npm i`.
-   Replace the `globalL0Url`, `metagraphL1DataUrl`, and `privateKey` variables with your values.
-   Run the script with `node send_data_transaction.js`.
-   Query the state GET endpoint at `<your metagraph L0 base url>/data-application/posts` to see the updated state after each update.

Persisting User Calculated State to PostgreSQL
----------------------------------------------

The metagraph will store the user-calculated state in an external PostgreSQL database. This PostgreSQL database requires a table named calculated_states with two columns: ordinal (number) and state (jsonb). The database credentials should be specified in the application.json file.

To set up PostgreSQL using Euclid, you can customize your Dockerfile. Starting from version v0.13.0, Euclid supports running custom Dockerfiles.

To do this, add the following content to a new file named `Dockerfile` in the directory: `infra/docker/custom/metagraph-base-image`.
```dockerfile
ARG TESSELLATION_VERSION_NAME

FROM metagraph-ubuntu-${TESSELLATION_VERSION_NAME}

ARG SHOULD_BUILD_GLOBAL_L0
ARG SHOULD_BUILD_DAG_L1
ARG SHOULD_BUILD_METAGRAPH_L0
ARG SHOULD_BUILD_CURRENCY_L1
ARG SHOULD_BUILD_DATA_L1
ARG TEMPLATE_NAME

ENV LC_ALL C.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

COPY project/$TEMPLATE_NAME $TEMPLATE_NAME
COPY global-l0/genesis/genesis.csv global-genesis.csv
COPY metagraph-l0/genesis/genesis.csv metagraph-genesis.csv

RUN mkdir shared_jars && mkdir shared_genesis

RUN apt-get update && \
    apt-get install -y \
    postgresql postgresql-contrib curl && \
    apt-get clean

RUN set -e; \
    if [ "$SHOULD_BUILD_GLOBAL_L0" = "true" ]; then \
        mkdir global-l0 && \
        cp global-l0.jar global-l0/global-l0.jar && \
        cp cl-wallet.jar global-l0/cl-wallet.jar && \
        cp cl-keytool.jar global-l0/cl-keytool.jar && \
        mv global-genesis.csv global-l0/genesis.csv; \
    fi

RUN set -e; \
    if [ "$SHOULD_BUILD_DAG_L1" = "true" ]; then \
        mkdir dag-l1 && \
        cp dag-l1.jar dag-l1/dag-l1.jar && \
        cp cl-wallet.jar dag-l1/cl-wallet.jar && \
        cp cl-keytool.jar dag-l1/cl-keytool.jar; \
    fi

RUN set -e; \
    if [ "$SHOULD_BUILD_METAGRAPH_L0" = "true" ]; then \
        mkdir metagraph-l0 && \
        cp cl-wallet.jar metagraph-l0/cl-wallet.jar && \
        cp cl-keytool.jar metagraph-l0/cl-keytool.jar && \
        rm -r -f $TEMPLATE_NAME/modules/l0/target && \
        cd $TEMPLATE_NAME && \
        sbt currencyL0/assembly && \
        cd .. && \
        mv $TEMPLATE_NAME/modules/l0/target/scala-2.13/*.jar metagraph-l0/metagraph-l0.jar && \
        mv metagraph-genesis.csv metagraph-l0/genesis.csv && \
        cp metagraph-l0/metagraph-l0.jar shared_jars/metagraph-l0.jar && \
        cp metagraph-l0/genesis.csv shared_genesis/genesis.csv; \
    fi

RUN set -e; \
    if [ "$SHOULD_BUILD_CURRENCY_L1" = "true" ]; then \
        mkdir currency-l1 && \
        cp cl-wallet.jar currency-l1/cl-wallet.jar && \
        cp cl-keytool.jar currency-l1/cl-keytool.jar && \
        rm -r -f $TEMPLATE_NAME/modules/l1/target && \
        cd $TEMPLATE_NAME && \
        sbt currencyL1/assembly && \
        cd .. && \
        mv $TEMPLATE_NAME/modules/l1/target/scala-2.13/*.jar currency-l1/currency-l1.jar && \
        cp currency-l1/currency-l1.jar shared_jars/currency-l1.jar; \
    fi

RUN set -e; \
    if [ "$SHOULD_BUILD_DATA_L1" = "true" ]; then \
        mkdir data-l1 && \
        cp cl-wallet.jar data-l1/cl-wallet.jar && \
        cp cl-keytool.jar data-l1/cl-keytool.jar && \
        rm -r -f $TEMPLATE_NAME/modules/data_l1/target && \
        cd $TEMPLATE_NAME && \
        sbt dataL1/assembly && \
        cd .. && \
        mv $TEMPLATE_NAME/modules/data_l1/target/scala-2.13/*.jar data-l1/data-l1.jar && \
        cp data-l1/data-l1.jar shared_jars/data-l1.jar; \
    fi

RUN rm -r -f cl-keytool.jar && \
    rm -r -f cl-wallet.jar && \
    rm -r -f global-l0.jar && \
    rm -r -f dag-l1.jar && \
    rm -r -f global-genesis.csv && \
    rm -r -f metagraph-genesis.csv && \
    rm -r -f tessellation && \
    rm -r -f $TEMPLATE_NAME

    # Environment variables for PostgreSQL
ENV POSTGRES_USER=social \
POSTGRES_PASSWORD=social \
POSTGRES_DB=social

# Expose PostgreSQL port
EXPOSE 5432

# Create the directory for init scripts and add the init.sql script
RUN mkdir -p /docker-entrypoint-initdb.d && \
    echo "CREATE TABLE calculated_states ( \
             ordinal BIGINT PRIMARY KEY, \
             state JSONB \
         );" > /docker-entrypoint-initdb.d/init.sql && \
    echo "GRANT ALL PRIVILEGES ON TABLE calculated_states TO $POSTGRES_USER;" >> /docker-entrypoint-initdb.d/init.sql

# Start PostgreSQL and create the necessary table
CMD service postgresql start && \
    su - postgres -c "psql -c \"CREATE USER $POSTGRES_USER WITH PASSWORD '$POSTGRES_PASSWORD';\"" && \
    su - postgres -c "psql -c \"CREATE DATABASE $POSTGRES_DB WITH OWNER $POSTGRES_USER;\"" && \
    su - postgres -c "psql $POSTGRES_DB -f /docker-entrypoint-initdb.d/init.sql" && \
    tail -f /dev/null
```

This Dockerfile will install postgres individually in each container/node.

This setup ensures that the user calculated state is persisted in the PostgreSQL database, providing a reliable and scalable way to store and retrieve state data.