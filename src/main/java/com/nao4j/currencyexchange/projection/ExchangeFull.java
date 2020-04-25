package com.nao4j.currencyexchange.projection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.nao4j.currencyexchange.validation.CurrencyCode;
import com.nao4j.currencyexchange.validation.Rate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
public record ExchangeFull(
        @CurrencyCode String from,
        @CurrencyCode String to,
        @Rate BigDecimal rate,
        ZonedDateTime time
) {}
