package com.nao4j.currencyexchange.converter;

import com.nao4j.currencyexchange.domain.Exchange;

import java.time.ZoneId;

public interface ExchangeToProjectionConverter {

    enum Projection {
        FULL,
        SHORT
    }

    Object convert(Exchange exchange, ZoneId zone, Projection projection);

}
