CREATE SCHEMA IF NOT EXISTS tradiebids;
create table if not exists tradies
(
    tradie_id            SERIAL PRIMARY KEY NOT NULL,
    first_name           VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NULL,
    email      VARCHAR(100) NULL,
    mobile     VARCHAR(100) NULL
);

create table if not exists customers
(
    customer_id     SERIAL PRIMARY KEY NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NULL,
    email      VARCHAR(100) NULL,
    mobile     VARCHAR(100) NULL
);

create table if not exists jobs
(
    job_id            SERIAL PRIMARY KEY NOT NULL,
    customer_id       INT NOT NULL REFERENCES customers,
    description       VARCHAR,
    expected_hours    INT NOT NULL,
    winning_bid_id    INT NULL,
    to_be_bidden_by   TIMESTAMP    NOT NULL
);

create table if not exists bids
(
    bid_id        SERIAL PRIMARY KEY NOT NULL,
    job_id        INT NOT NULL REFERENCES jobs,
    tradie_id     INT NOT NULL REFERENCES tradies,
    fixed_price   INT NULL,
    hourly_rate   INT NULL,
    unique (job_id, tradie_id)
);