package com.nao4j.currencyexchange.projection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
public record ExchangeFull(String from, String to, BigDecimal rate, ZonedDateTime time) {}
