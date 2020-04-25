package com.nao4j.currencyexchange.service;

import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.exception.Failure;
import com.nao4j.currencyexchange.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final long expireInMinutes;

    public CurrencyServiceImpl(
            final CurrencyRepository currencyRepository,
            @Value("${currency-exchange-service.expire-in-minutes}") final long expireInMinutes
    ) {
        this.currencyRepository = currencyRepository;
        this.expireInMinutes = expireInMinutes;
    }

    @Override
    @Transactional
    public List<Currency> getAllByTime(final LocalDateTime time) {
        final var start = time.minusMinutes(expireInMinutes);
        return currencyRepository.findAllActualInPeriod(start, time);
    }
    @Override
    @Transactional
    public List<Currency> getAllByTimeNonStrict(final LocalDateTime time) {
        return currencyRepository.findAllUpToTime(time);
    }

    @Override
    @Transactional
    public Currency create(final String code, final int quantifier) {
        if (currencyRepository.existsByCode(code)) {
            throw new Failure(format("Currency '%s' already exists", code));
        }
        return currencyRepository.save(new Currency(code, quantifier));
    }

    @Override
    @Transactional
    public Currency update(final String code, final int quantifier) {
        final var idContainer = currencyRepository.findByCode(code).map(Currency::getId);
        if (idContainer.isEmpty()) {
            throw new Failure(format("Currency '%s' not exists", code));
        }
        return currencyRepository.save(new Currency(idContainer.get(), code, quantifier));
    }

}
