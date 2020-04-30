package com.nao4j.currencyexchange.converter;

import com.nao4j.currencyexchange.converter.ExchangeToProjectionConverter.Projection;
import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.domain.Exchange;
import com.nao4j.currencyexchange.projection.ExchangeFull;
import com.nao4j.currencyexchange.projection.ExchangeShort;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.nao4j.currencyexchange.TestUtil.defaultZoneOffset;

class ExchangeToProjectionConverterTest implements WithAssertions {

    ExchangeToProjectionConverter converter;

    @BeforeEach
    void setup() {
        converter = new ExchangeToProjectionConverterImpl();
    }

    @Test
    void shouldPassForFullProjection() {
        final var zone = ZoneId.systemDefault();
        final var from = new Currency("RUB", 4);
        final var to = new Currency("BTC", 8);
        final var rate = new BigDecimal("0.0000018000");
        final var time = LocalDateTime.parse("2020-04-26T14:10");
        final var exchange = new Exchange(5L, from, to, rate, time);

        final var expected = new ExchangeFull("RUB", "BTC", new BigDecimal("0.00000180"), ZonedDateTime.of(time, zone));
        assertThat(converter.convert(exchange, zone, Projection.FULL)).isEqualTo(expected);
    }

    @Test
    void shouldPassForFullProjectionAndNotDefaultTimeZone() {
        final var zone = defaultZoneOffset(-3);
        final var from = new Currency("RUB", 4);
        final var to = new Currency("BTC", 8);
        final var rate = new BigDecimal("0.0000018000");
        final var time = LocalDateTime.parse("2020-04-26T14:10");
        final var exchange = new Exchange(5L, from, to, rate, time);

        final var expectedTime = ZonedDateTime.of(LocalDateTime.parse("2020-04-26T11:10"), zone);
        final var expected = new ExchangeFull("RUB", "BTC", new BigDecimal("0.00000180"), expectedTime);
        assertThat(converter.convert(exchange, zone, Projection.FULL)).isEqualTo(expected);
    }

    @Test
    void shouldPassForShortProjection() {
        final var zone = ZoneId.systemDefault();
        final var from = new Currency("RUB", 4);
        final var to = new Currency("BTC", 8);
        final var rate = new BigDecimal("0.0000018000");
        final var time = LocalDateTime.parse("2020-04-26T14:10");
        final var exchange = new Exchange(5L, from, to, rate, time);

        final var expected = new ExchangeShort(new BigDecimal("0.00000180"), ZonedDateTime.of(time, zone));
        assertThat(converter.convert(exchange, zone, Projection.SHORT)).isEqualTo(expected);
    }

    @Test
    void shouldPassForShortProjectionAndNotDefaultTimeZone() {
        final var zone = defaultZoneOffset(-3);
        final var from = new Currency("RUB", 4);
        final var to = new Currency("BTC", 8);
        final var rate = new BigDecimal("0.0000018000");
        final var time = LocalDateTime.parse("2020-04-26T14:10");
        final var exchange = new Exchange(5L, from, to, rate, time);

        final var expectedTime = ZonedDateTime.of(LocalDateTime.parse("2020-04-26T11:10"), zone);
        final var expected = new ExchangeShort(new BigDecimal("0.00000180"), expectedTime);
        assertThat(converter.convert(exchange, zone, Projection.SHORT)).isEqualTo(expected);
    }

}
