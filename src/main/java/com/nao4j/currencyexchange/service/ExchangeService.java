package com.nao4j.currencyexchange.service;

import com.nao4j.currencyexchange.domain.Exchange;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ExchangeService {

    Optional<Exchange> getByTime(String codeFrom, String codeTo, LocalDateTime time);

    Optional<Exchange> getByTimeNonStrict(String codeFrom, String codeTo, LocalDateTime time);

    Exchange create(String codeFrom, String codeTo, BigDecimal rate, LocalDateTime time);

}
