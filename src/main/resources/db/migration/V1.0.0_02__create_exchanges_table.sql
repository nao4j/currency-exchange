CREATE SEQUENCE currency_exchange.exchanges_id_seq
    MINVALUE 1
    INCREMENT BY 1
    NO CYCLE;

CREATE TABLE currency_exchange.exchanges(
    id      BIGINT          NOT NULL PRIMARY KEY DEFAULT nextval('currency_exchange.exchanges_id_seq'),
    from_id SERIAL          NOT NULL REFERENCES currency_exchange.currencies (id),
    to_id   SERIAL          NOT NULL REFERENCES currency_exchange.currencies (id),
    rate    NUMERIC(19, 10) NOT NULL,
    time    TIMESTAMP       NOT NULL
);
