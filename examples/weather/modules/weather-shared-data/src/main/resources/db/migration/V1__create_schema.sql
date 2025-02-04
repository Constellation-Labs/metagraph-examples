CREATE TABLE IF NOT EXISTS locations
(
    id      VARCHAR PRIMARY KEY,
    name    VARCHAR NOT NULL,
    region  VARCHAR NOT NULL,
    country VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS weather_records
(
    id               VARCHAR PRIMARY KEY,
    snapshot_ordinal BIGINT    NOT NULL,
    location_id      VARCHAR   NOT NULL REFERENCES locations (id) ON DELETE CASCADE,
    temp_f           FLOAT     NOT NULL,
    temp_c           FLOAT     NOT NULL,
    condition        VARCHAR   NOT NULL CHECK (condition IN
                                               ('Sunny', 'PartlyCloudy', 'Cloudy', 'Rainy', 'Snowy', 'Stormy', 'Turbulent', 'Unknown')),
    date             TIMESTAMP NOT NULL
);
