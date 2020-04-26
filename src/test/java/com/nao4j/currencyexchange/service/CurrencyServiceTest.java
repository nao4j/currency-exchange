package com.nao4j.currencyexchange.service;

import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.exception.Failure;
import com.nao4j.currencyexchange.repository.CurrencyRepository;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest implements WithAssertions {

    static final long EXPIRE_IN_MINUTES = 1440;

    @Mock
    CurrencyRepository currencyRepository;

    CurrencyService currencyService;

    Object[] mocks;

    @BeforeEach
    void setup() {
        currencyService = new CurrencyServiceImpl(currencyRepository, EXPIRE_IN_MINUTES);
        mocks = new Object[] {currencyRepository};
    }

    @Nested
    class GetAllByTime {

        @Test
        void shouldPass(@Mock final Currency currency1, @Mock final Currency currency2) {
            when(currencyRepository.findAllActualInPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(currency1, currency2));

            assertThat(currencyService.getAllByTime(LocalDateTime.parse("2020-04-26T14:50")))
                    .containsExactly(currency1, currency2);

            verify(currencyRepository).findAllActualInPeriod(
                    LocalDateTime.parse("2020-04-25T14:50"), LocalDateTime.parse("2020-04-26T14:50")
            );
            verifyNoMoreInteractions(currencyRepository);
        }

    }

    @Nested
    class GetAllByTimeNonStrict {

        @Test
        void shouldPass(@Mock final Currency currency1, @Mock final Currency currency2) {
            when(currencyRepository.findAllUpToTime(any(LocalDateTime.class)))
                    .thenReturn(List.of(currency1, currency2));

            assertThat(currencyService.getAllByTimeNonStrict(LocalDateTime.parse("2020-04-26T14:50")))
                    .containsExactly(currency1, currency2);

            verify(currencyRepository).findAllUpToTime(LocalDateTime.parse("2020-04-26T14:50"));
            verifyNoMoreInteractions(currencyRepository);
        }

    }

    @Nested
    class Create {

        @Test
        void shouldPass() {
            when(currencyRepository.existsByCode(anyString())).thenReturn(false);
            when(currencyRepository.saveAndFlush(any(Currency.class))).thenAnswer(returnsFirstArg());

            final var expected = new Currency("USD", 4);
            assertThat(currencyService.create("USD", 4)).isEqualTo(expected);

            verify(currencyRepository).existsByCode("USD");
            verify(currencyRepository).saveAndFlush(expected);
            verifyNoMoreInteractions(currencyRepository);
        }

        @Test
        void shouldFailIfCodeIsNotUnique() {
            when(currencyRepository.existsByCode(anyString())).thenReturn(true);

            assertThatThrownBy(() -> currencyService.create("USD", 4))
                    .isExactlyInstanceOf(Failure.class)
                    .hasMessage("Currency 'USD' already exists");

            verify(currencyRepository).existsByCode("USD");
            verifyNoMoreInteractions(currencyRepository);
        }

    }

    @Nested
    class Update {

        @Test
        void shouldPass(@Mock final Currency currency) {
            when(currency.getId()).thenReturn(2L);
            when(currencyRepository.findByCode(anyString())).thenReturn(Optional.of(currency));
            final var expected = new Currency(2L, "USD", 2);
            when(currencyRepository.saveAndFlush(any(Currency.class))).thenAnswer(returnsFirstArg());

            assertThat(currencyService.update("USD", 2)).isEqualTo(expected);

            verify(currencyRepository).findByCode("USD");
            verify(currencyRepository).saveAndFlush(expected);
            verifyNoMoreInteractions(currencyRepository);
        }

        @Test
        void shouldFailIfNotFound() {
            when(currencyRepository.findByCode(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> currencyService.update("USD", 2))
                    .isExactlyInstanceOf(Failure.class)
                    .hasMessage("Currency 'USD' not exists");

            verify(currencyRepository).findByCode("USD");
            verifyNoMoreInteractions(currencyRepository);
        }

    }

}
