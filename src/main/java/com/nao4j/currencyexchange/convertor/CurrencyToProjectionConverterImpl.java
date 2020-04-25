package com.nao4j.currencyexchange.convertor;

import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.projection.CurrencyFull;
import org.springframework.stereotype.Component;

@Component
public class CurrencyToProjectionConverterImpl implements CurrencyToProjectionConverter {

    @Override
    public Object convert(final Currency currency, final Projection projection) {
        return switch (projection) {
            case FULL -> new CurrencyFull(currency.getCode(), currency.getQuantifier());
            case SHORT -> currency.getCode();
        };
    }

}
