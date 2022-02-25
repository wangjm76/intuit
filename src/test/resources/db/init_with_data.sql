CREATE SCHEMA IF NOT EXISTS tradiebids;
create table if not exists tradies
(
    tradie_id  SERIAL PRIMARY KEY NOT NULL,
    first_name VARCHAR(100)       NOT NULL,
    last_name  VARCHAR(100)       NULL,
    email      VARCHAR(100)       NULL,
    mobile     VARCHAR(100)       NULL
);

create table if not exists customers
(
    customer_id SERIAL PRIMARY KEY NOT NULL,
    first_name  VARCHAR(100)       NOT NULL,
    last_name   VARCHAR(100)       NULL,
    email       VARCHAR(100)       NULL,
    mobile      VARCHAR(100)       NULL
);

create table if not exists jobs
(
    job_id          SERIAL PRIMARY KEY NOT NULL,
    customer_id     INT                NOT NULL REFERENCES customers,
    description     VARCHAR,
    expected_hours  INT                NOT NULL,
    winning_bid_id  INT                NULL,
    to_be_bidden_by TIMESTAMP          NOT NULL
);

create table if not exists bids
(
    bid_id      SERIAL PRIMARY KEY NOT NULL,
    job_id      INT                NOT NULL REFERENCES jobs,
    tradie_id   INT                NOT NULL REFERENCES tradies,
    fixed_price INT                NULL,
    hourly_rate INT                NULL,
    unique (job_id, tradie_id)
);

-- TradeTwo will win the bid as total cost is 100 < fix price 200
insert into customers
values (1001, 'James', 'Wang', 'james.wang@test.com', '04222222222');
insert into tradies
values (1002, 'Tradie', 'One', 'tradieOne@test.com', '04222222222');
insert into tradies
values (1003, 'Tradie', 'Two', 'tradieTwo@test.com', '04222222222');
insert into jobs
values (1004, 1001, 'Job1', 2, null, NOW());
insert into bids
values (1005, 1004, 1002, 200, null);
insert into bids
values (1006, 1004, 1003, null, 50);

-- no bid for the job
insert into jobs
values (1007, 1001, 'Job2', 5, null, NOW());