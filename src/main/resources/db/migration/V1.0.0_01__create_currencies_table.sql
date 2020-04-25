CREATE SEQUENCE currency_exchange.currencies_id_seq
    MINVALUE 1
    INCREMENT BY 1
    NO CYCLE;

CREATE TABLE currency_exchange.currencies(
    id         BIGINT     NOT NULL PRIMARY KEY DEFAULT nextval('currency_exchange.currencies_id_seq'),
    code       VARCHAR(3) NOT NULL UNIQUE,
    quantifier INT        NOT NULL
);
