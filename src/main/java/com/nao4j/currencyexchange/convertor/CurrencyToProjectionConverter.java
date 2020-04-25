package com.nao4j.currencyexchange.convertor;

import com.nao4j.currencyexchange.domain.Currency;

public interface CurrencyToProjectionConverter {

    enum Projection {
        FULL,
        SHORT
    }

    Object convert(Currency currency, Projection projection);

}
