package com.nao4j.currencyexchange.projection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.nao4j.currencyexchange.validation.CurrencyCode;
import com.nao4j.currencyexchange.validation.Quantifier;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
public record CurrencyFull(@CurrencyCode String code, @Quantifier int quantifier) {}
