package com.nao4j.currencyexchange.convertor;

import com.nao4j.currencyexchange.domain.Exchange;
import com.nao4j.currencyexchange.projection.ExchangeFull;
import com.nao4j.currencyexchange.projection.ExchangeShort;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.math.RoundingMode.HALF_DOWN;

@Component
public class ExchangeToProjectionConverterImpl implements ExchangeToProjectionConverter {

    @Override
    public Object convert(final Exchange exchange, final ZoneId zone, final Projection projection) {
        final var from = exchange.getFrom().getCode();
        final var to = exchange.getTo().getCode();
        final var rate = exchange.getRate().setScale(exchange.getTo().getQuantifier(), HALF_DOWN);
        final var time = ZonedDateTime.of(exchange.getTime(), ZoneId.systemDefault()).withZoneSameInstant(zone);
        return switch (projection) {
            case FULL -> new ExchangeFull(from, to, rate, time);
            case SHORT -> new ExchangeShort(rate, time);
        };
    }

}
