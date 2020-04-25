package com.nao4j.currencyexchange.service;

import com.nao4j.currencyexchange.domain.Currency;

import java.time.LocalDateTime;
import java.util.List;

public interface CurrencyService {

    List<Currency> getAllByTime(LocalDateTime time);

    List<Currency> getAllByTimeNonStrict(LocalDateTime time);

    Currency create(String code, int quantifier);

    Currency update(String code, int quantifier);

}
