package com.nao4j.currencyexchange.service;

import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.domain.Exchange;
import com.nao4j.currencyexchange.exception.Failure;
import com.nao4j.currencyexchange.repository.CurrencyRepository;
import com.nao4j.currencyexchange.repository.ExchangeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_DOWN;
import static java.time.Month.JANUARY;
import static java.util.stream.Collectors.toSet;

@Service
class ExchangeServiceImpl implements ExchangeService {

    private final ExchangeRepository exchangeRepository;
    private final CurrencyRepository currencyRepository;
    private final long expireInMinutes;

    public ExchangeServiceImpl(
            final ExchangeRepository exchangeRepository,
            final CurrencyRepository currencyRepository,
            @Value("${currency-exchange-service.expire-in-minutes}") final long expireInMinutes
    ) {
        this.exchangeRepository = exchangeRepository;
        this.currencyRepository = currencyRepository;
        this.expireInMinutes = expireInMinutes;
    }

    @Override
    @Transactional
    public Optional<Exchange> getByTime(final String codeFrom, final String codeTo, final LocalDateTime time) {
        final var start = time.minusMinutes(expireInMinutes);
        return getByPeriod(codeFrom, codeTo, start, time);
    }

    @Override
    @Transactional
    public Optional<Exchange> getByTimeNonStrict(final String codeFrom, final String codeTo, final LocalDateTime time) {
        final var start = LocalDateTime.of(2000, JANUARY, 1, 0, 0);
        return getByPeriod(codeFrom, codeTo, start, time);
    }

    @Override
    @Transactional
    public Exchange create(
            final String codeFrom,
            final String codeTo,
            final BigDecimal rate,
            final LocalDateTime time
    ) {
        final int quantifier = getRealScale(rate);
        final var from = findOrCreate(codeFrom, quantifier);
        final var to = findOrCreate(codeTo, quantifier);
        return exchangeRepository.saveAndFlush(new Exchange(from, to, rate, time));
    }

    private Optional<Exchange> getByPeriod(
            final String codeFrom,
            final String codeTo,
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        if (codeFrom.equals(codeTo)) {
            throw new IllegalArgumentException("Сan not convert currency into itself");
        }
        final var result = exchangeRepository.findFirstByFromCodeAndToCodeAndTimeBetweenOrderByTimeDesc(
                codeFrom, codeTo, start, end
        );
        return result.isPresent() ? result : generate(codeFrom, codeTo, start, end);
    }

    private Optional<Exchange> generate(
            final String codeFrom,
            final String codeTo,
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        final var reverseResult = generateByReverse(codeFrom, codeTo, start, end);
        return reverseResult.isPresent() ? reverseResult : generateByIntermediate(codeFrom, codeTo, start, end);
    }

    private Optional<Exchange> generateByReverse(
            final String codeFrom,
            final String codeTo,
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        final var reverseContainer = exchangeRepository.findFirstByFromCodeAndToCodeAndTimeBetweenOrderByTimeDesc(
                codeTo, codeFrom, start, end
        );
        if (reverseContainer.isEmpty()) {
            return Optional.empty();
        }

        final var reverse = reverseContainer.get();
        final var from = reverse.getTo();
        final var to = reverse.getFrom();
        final var time = reverse.getTime();
        final var rate = ONE.divide(reverse.getRate(), 10, HALF_DOWN);
        final var result = new Exchange(from, to, rate, time);
        return Optional.of(exchangeRepository.saveAndFlush(result));
    }

    private Optional<Exchange> generateByIntermediate(
            final String codeFrom, final String codeTo, final LocalDateTime start, final LocalDateTime end
    ) {
        final var allByFrom = exchangeRepository.findTop1000ByToCodeAndTimeBetweenOrderByTimeDesc(codeFrom, start, end);
        if (allByFrom.isEmpty()) {
            return Optional.empty();
        }
        final var allByTo = exchangeRepository.findTop1000ByToCodeAndTimeBetweenOrderByTimeDesc(codeTo, start, end);
        if (allByTo.isEmpty()) {
            return Optional.empty();
        }

        final var allBaseForFrom = allByFrom.stream().map(Exchange::getFrom).map(Currency::getCode).collect(toSet());
        final var allBaseForTo = allByTo.stream().map(Exchange::getFrom).map(Currency::getCode).collect(toSet());

        final var commonBaseContainer = findCommonBase(allBaseForFrom, allBaseForTo);
        if (commonBaseContainer.isEmpty()) {
            return Optional.empty();
        }
        final var commonBase = commonBaseContainer.get();

        final var firstFrom = extractFirstWithBase(allByFrom, commonBase);
        final var firstTo = extractFirstWithBase(allByTo, commonBase);

        final var rate = firstTo.getRate().divide(firstFrom.getRate(), 10, HALF_DOWN);
        final var time = firstFrom.getTime().compareTo(firstTo.getTime()) <= 0
                ? firstFrom.getTime()
                : firstTo.getTime();
        final var result = new Exchange(firstFrom.getTo(), firstTo.getTo(), rate, time);
        return Optional.of(exchangeRepository.saveAndFlush(result));
    }

    private Optional<String> findCommonBase(final Set<String> baseFromSet, final Set<String> baseToSet) {
        for (String base : baseFromSet) {
            if (baseToSet.contains(base)) {
                return Optional.of(base);
            }
        }
        return Optional.empty();
    }

    private Exchange extractFirstWithBase(final Collection<Exchange> exchanges, final String baseCode) {
        for (Exchange exchange : exchanges) {
            if (baseCode.equals(exchange.getFrom().getCode())) {
                return exchange;
            }
        }
        throw new Failure("Collection must contain records with baseCode");
    }

    private Currency findOrCreate(final String code, final int quantifier) {
        final var resultContainer = currencyRepository.findByCode(code);
        if (resultContainer.isEmpty()) {
            return currencyRepository.saveAndFlush(new Currency(code, quantifier));
        }
        return resultContainer.get();
    }

    private int getRealScale(final BigDecimal rate) {
        return BigDecimal.valueOf(rate.doubleValue()).scale();
    }

}
