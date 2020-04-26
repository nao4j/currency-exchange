package com.nao4j.currencyexchange.converter;

import com.nao4j.currencyexchange.domain.Currency;

public interface CurrencyToProjectionConverter {

    enum Projection {
        FULL,
        SHORT
    }

    Object convert(Currency currency, Projection projection);

}
