package com.nao4j.currencyexchange.repository;

import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.domain.Exchange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static java.math.BigDecimal.ONE;

class CurrencyRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    CurrencyRepository currencyRepository;

    @Test
    void shouldPassForExistsByCode() {
        createCurrency("USD", 4);

        assertThat(currencyRepository.existsByCode("USD")).isTrue();
    }

    @Test
    void shouldPassForExistsByCodeIfNotExists() {
        assertThat(currencyRepository.existsByCode("USD")).isFalse();
    }

    @Test
    void shouldPassForFindByCode() {
        createCurrency("RUB", 4);
        createCurrency("EUR", 4);
        final var expected = createCurrency("USD", 2);

        assertThat(currencyRepository.findByCode("USD")).isPresent().get().isEqualTo(expected);
    }

    @Test
    void shouldPassForFindByCodeIfNotExists() {
        createCurrency("RUB", 4);
        createCurrency("EUR", 4);

        assertThat(currencyRepository.findByCode("USD")).isEmpty();
    }

    @Test
    void shouldPassForFindAllActualInPeriod() {
        final var rub = createCurrency("RUB", 4);
        final var usd = createCurrency("USD", 4);
        final var eur = createCurrency("EUR", 4);
        createExchange(rub, usd, "2020-04-26T19:30");
        createExchange(usd, rub, "2020-04-26T19:30");
        createExchange(usd, rub, "2020-04-26T19:31");
        createExchange(eur, usd, "2020-04-24T19:30");
        createExchange(usd, eur, "2020-04-27T19:30");

        final var start = LocalDateTime.parse("2020-04-25T00:00");
        final var end = LocalDateTime.parse("2020-04-26T23:59");
        assertThat(currencyRepository.findAllActualInPeriod(start, end)).containsExactly(rub, usd);
    }

    @Test
    void shouldPassForFindAllActualInPeriodIfNotExists() {
        final var rub = createCurrency("RUB", 4);
        final var usd = createCurrency("USD", 4);
        final var eur = createCurrency("EUR", 4);
        createExchange(rub, usd, "2020-04-23T19:30");
        createExchange(usd, rub, "2020-04-23T19:30");
        createExchange(usd, rub, "2020-04-23T19:31");
        createExchange(eur, usd, "2020-04-24T19:30");
        createExchange(usd, eur, "2020-04-27T19:30");

        final var start = LocalDateTime.parse("2020-04-25T00:00");
        final var end = LocalDateTime.parse("2020-04-26T23:59");
        assertThat(currencyRepository.findAllActualInPeriod(start, end)).isEmpty();
    }

    @Test
    void shouldPassForFindAllUpToTime() {
        final var rub = createCurrency("RUB", 4);
        final var usd = createCurrency("USD", 4);
        final var eur = createCurrency("EUR", 4);
        createExchange(rub, usd, "2020-04-23T19:30");
        createExchange(usd, rub, "2020-04-23T19:30");
        createExchange(usd, rub, "2020-04-23T19:31");
        createExchange(eur, usd, "2020-04-24T19:30");
        createExchange(usd, eur, "2020-04-27T19:30");

        final var time = LocalDateTime.parse("2020-04-23T23:59");
        assertThat(currencyRepository.findAllUpToTime(time)).containsExactly(rub, usd);
    }

    @Test
    void shouldPassForFindAllUpToTimeIfNotExists() {
        final var rub = createCurrency("RUB", 4);
        final var usd = createCurrency("USD", 4);
        final var eur = createCurrency("EUR", 4);
        createExchange(rub, usd, "2020-04-23T19:30");
        createExchange(usd, rub, "2020-04-23T19:30");
        createExchange(usd, rub, "2020-04-23T19:31");
        createExchange(eur, usd, "2020-04-24T19:30");
        createExchange(usd, eur, "2020-04-27T19:30");

        final var time = LocalDateTime.parse("2020-04-22T23:59");
        assertThat(currencyRepository.findAllUpToTime(time)).isEmpty();
    }

    @Test
    void shouldPassForSaveAndFlush() {
        createCurrency("BTC", 8);
        final var currency = new Currency("USD", 4);

        assertThat(currencyRepository.saveAndFlush(currency)).isEqualToIgnoringGivenFields(currency, "id");
    }

    @Test
    void shouldFailForSaveAndFlushIfNotUnique() {
        createCurrency("BTC", 8);

        assertThatThrownBy(() -> currencyRepository.saveAndFlush(new Currency("BTC", 4)))
                .isExactlyInstanceOf(DataIntegrityViolationException.class);
    }

    private Currency createCurrency(final String code, final int quantifier) {
        return entityManager.persistAndFlush(new Currency(code, quantifier));
    }

    private void createExchange(final Currency from, final Currency to, final String time) {
        entityManager.persistAndFlush(new Exchange(from, to, ONE, LocalDateTime.parse(time)));
    }

}
