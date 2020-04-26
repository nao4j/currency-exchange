package com.nao4j.currencyexchange.converter;

import com.nao4j.currencyexchange.converter.CurrencyToProjectionConverter.Projection;
import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.projection.CurrencyFull;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CurrencyToProjectionConverterTest implements WithAssertions {

    CurrencyToProjectionConverter converter;

    @BeforeEach
    void setup() {
        converter = new CurrencyToProjectionConverterImpl();
    }

    @Test
    void shouldPassForFullProjection() {
        assertThat(converter.convert(new Currency(4L, "USD", 4), Projection.FULL))
                .isEqualTo(new CurrencyFull("USD", 4));
    }

    @Test
    void shouldPassForShortProjection() {
        assertThat(converter.convert(new Currency(4L, "USD", 4), Projection.SHORT)).isEqualTo("USD");
    }

}
