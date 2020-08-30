package com.nao4j.currencyexchange.service;

import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.domain.Exchange;
import com.nao4j.currencyexchange.repository.CurrencyRepository;
import com.nao4j.currencyexchange.repository.ExchangeRepository;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest implements WithAssertions {

    static final long EXPIRE_IN_MINUTES = 120;

    @Mock
    ExchangeRepository exchangeRepository;

    @Mock
    CurrencyRepository currencyRepository;

    ExchangeService exchangeService;

    Object[] mocks;

    @BeforeEach
    void setup() {
        exchangeService = new ExchangeServiceImpl(exchangeRepository, currencyRepository, EXPIRE_IN_MINUTES);
        mocks = new Object[] {exchangeRepository, currencyRepository};
    }

    @Nested
    class GetByTime {

        @Test
        void shouldPass() {
            final var from = new Currency(4L, "USD", 2);
            final var to = new Currency(5L, "RUB", 2);
            final var time = LocalDateTime.parse("2020-08-30T14:00");
            final var exchange = new Exchange(1L, from, to, new BigDecimal("74.3"), time.minusMinutes(5));
            when(exchangeRepository.findFirstByFromCodeAndToCodeAndTimeBetweenOrderByTimeDesc(
                    anyString(),
                    anyString(),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(Optional.of(exchange));

            assertThat(exchangeService.getByTime(from.getCode(), to.getCode(), time))
                    .isPresent()
                    .get().isEqualTo(exchange);

            verify(exchangeRepository).findFirstByFromCodeAndToCodeAndTimeBetweenOrderByTimeDesc(
                    from.getCode(),
                    to.getCode(),
                    time.minusMinutes(EXPIRE_IN_MINUTES),
                    time
            );
            verifyNoMoreInteractions(mocks);
        }

        //TODO: реализовать тесты

        @Test
        void shouldFailIfFromEqualsTo() {
            assertThatThrownBy(() -> exchangeService.getByTime("USD", "USD", LocalDateTime.parse("2020-08-30T14:00")))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Сan not convert currency into itself");

            verifyNoMoreInteractions(mocks);
        }

    }

    @Nested
    class GetByTimeNonStrict {

        //TODO: реализовать тесты

    }

    @Nested
    class Create {

        //TODO: реализовать тесты

    }

}
