package com.nao4j.currencyexchange.controller;

import com.nao4j.currencyexchange.converter.CurrencyToProjectionConverter;
import com.nao4j.currencyexchange.converter.CurrencyToProjectionConverter.Projection;
import com.nao4j.currencyexchange.domain.Currency;
import com.nao4j.currencyexchange.exception.Failure;
import com.nao4j.currencyexchange.projection.CurrencyFull;
import com.nao4j.currencyexchange.service.CurrencyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.nao4j.currencyexchange.TestUtil.defaultZoneOffset;
import static com.nao4j.currencyexchange.converter.CurrencyToProjectionConverter.Projection.FULL;
import static com.nao4j.currencyexchange.converter.CurrencyToProjectionConverter.Projection.SHORT;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @MockBean
    CurrencyService currencyService;
    @MockBean
    CurrencyToProjectionConverter currencyToProjectionConverter;
    @Autowired
    MockMvc mockMvc;

    Object[] mocks;

    @BeforeEach
    void setup() {
        mocks = new Object[] {currencyService, currencyToProjectionConverter};
    }

    @AfterEach
    void cleanup() {
        // workaround: for use @MockBean with @Nested annotations
        reset(mocks);
    }

    @Nested
    class GetAll {

        @Test
        void shouldPassForFullActual() throws Exception {
            final var zone = defaultZoneOffset(-3);
            final var time = ZonedDateTime.of(LocalDateTime.parse("2020-04-30T14:38"), zone);
            final var currency1 = mock(Currency.class);
            final var currency2 = mock(Currency.class);
            when(currencyService.getAllByTime(any(LocalDateTime.class))).thenReturn(List.of(currency1, currency2));
            when(currencyToProjectionConverter.convert(any(Currency.class), any(Projection.class)))
                    .thenReturn(new CurrencyFull("USD", 4), new CurrencyFull("BTC", 8));

            mockMvc.perform(get("/currencies").param("time", time.format(ISO_DATE_TIME)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                        [{"code": "USD", "quantifier": 4}, {"code": "BTC", "quantifier": 8}]
                    """));

            verify(currencyService).getAllByTime(LocalDateTime.parse("2020-04-30T17:38"));
            verify(currencyToProjectionConverter).convert(currency1, FULL);
            verify(currencyToProjectionConverter).convert(currency2, FULL);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldPassForShortActual() throws Exception {
            final var zone = defaultZoneOffset(-3);
            final var time = ZonedDateTime.of(LocalDateTime.parse("2020-04-30T14:38"), zone);
            final var currency1 = mock(Currency.class);
            final var currency2 = mock(Currency.class);
            when(currencyService.getAllByTime(any(LocalDateTime.class))).thenReturn(List.of(currency1, currency2));
            when(currencyToProjectionConverter.convert(any(Currency.class), any(Projection.class)))
                    .thenReturn("USD", "BTC");

            mockMvc.perform(
                    get("/currencies")
                            .param("time", time.format(ISO_DATE_TIME))
                            .param("actualOnly", "true")
                            .param("projection", "SHORT")
            ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json("[\"USD\", \"BTC\"]"));

            verify(currencyService).getAllByTime(LocalDateTime.parse("2020-04-30T17:38"));
            verify(currencyToProjectionConverter).convert(currency1, SHORT);
            verify(currencyToProjectionConverter).convert(currency2, SHORT);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldPassForFullNonStrict() throws Exception {
            final var time = ZonedDateTime.of(LocalDateTime.parse("2020-04-30T14:38"), ZoneId.systemDefault());
            final var currency1 = mock(Currency.class);
            final var currency2 = mock(Currency.class);
            when(currencyService.getAllByTimeNonStrict(any(LocalDateTime.class)))
                    .thenReturn(List.of(currency1, currency2));
            when(currencyToProjectionConverter.convert(any(Currency.class), any(Projection.class)))
                    .thenReturn(new CurrencyFull("USD", 4), new CurrencyFull("BTC", 8));

            mockMvc.perform(
                    get("/currencies")
                            .param("time", time.format(ISO_DATE_TIME))
                            .param("actualOnly", "false")
                            .param("projection", "FULL")
            ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                        [{"code": "USD", "quantifier": 4}, {"code": "BTC", "quantifier": 8}]
                    """));

            verify(currencyService).getAllByTimeNonStrict(LocalDateTime.parse("2020-04-30T14:38"));
            verify(currencyToProjectionConverter).convert(currency1, FULL);
            verify(currencyToProjectionConverter).convert(currency2, FULL);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldPassForShortNonStrict() throws Exception {
            final var time = ZonedDateTime.of(LocalDateTime.parse("2020-04-30T14:38"), ZoneId.systemDefault());
            final var currency1 = mock(Currency.class);
            final var currency2 = mock(Currency.class);
            when(currencyService.getAllByTimeNonStrict(any(LocalDateTime.class)))
                    .thenReturn(List.of(currency1, currency2));
            when(currencyToProjectionConverter.convert(any(Currency.class), any(Projection.class)))
                    .thenReturn("USD", "BTC");

            mockMvc.perform(
                    get("/currencies")
                            .param("time", time.format(ISO_DATE_TIME))
                            .param("actualOnly", "false")
                            .param("projection", "SHORT")
            ).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json("[\"USD\", \"BTC\"]"));

            verify(currencyService).getAllByTimeNonStrict(LocalDateTime.parse("2020-04-30T14:38"));
            verify(currencyToProjectionConverter).convert(currency1, SHORT);
            verify(currencyToProjectionConverter).convert(currency2, SHORT);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldFailIfTimeNotProvided() throws Exception {
            mockMvc.perform(get("/currencies"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason("Required ZonedDateTime parameter 'time' is not present"));
        }

        @Test
        void shouldFailIfProjectionIsNotSupported() throws Exception {
            final var time = ZonedDateTime.now();

            mockMvc.perform(
                    get("/currencies")
                            .param("time", time.format(ISO_DATE_TIME))
                            .param("actualOnly", "true")
                            .param("projection", "WRONG_PROJECTION")
            ).andDo(print())
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class Create {

        @Test
        void shouldPass() throws Exception {
            mockMvc.perform(
                    post("/currencies")
                            .contentType(APPLICATION_JSON)
                            .content("{\"code\":\"USD\",\"quantifier\":4}")
            ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string(is(emptyString())));

            verify(currencyService).create("USD", 4);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldFailIfAlreadyExists() throws Exception {
            when(currencyService.create(anyString(), anyInt())).thenThrow(Failure.class);

            mockMvc.perform(
                    post("/currencies")
                            .contentType(APPLICATION_JSON)
                            .content("{\"code\":\"USD\",\"quantifier\":4}")
            ).andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(content().string(is(emptyString())));

            verify(currencyService).create("USD", 4);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldFailIfCodeIsInvalid() throws Exception {
            mockMvc.perform(
                    post("/currencies")
                            .contentType(APPLICATION_JSON)
                            .content("{\"code\":\"U\",\"quantifier\":4}")
            ).andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("code: Currency code must be 3-5 upper letter or numbers"));

            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldFailIfQuantifierIsInvalid() throws Exception {
            mockMvc.perform(
                    post("/currencies")
                            .contentType(APPLICATION_JSON)
                            .content("{\"code\":\"USD\",\"quantifier\":11}")
            ).andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("quantifier: Max quantifier is 10"));

            verifyNoMoreInteractions(mocks);
        }

    }

    @Nested
    class Update {

        @Test
        void shouldPass() throws Exception {
            mockMvc.perform(
                    put("/currencies")
                            .contentType(APPLICATION_JSON)
                            .content("{\"code\":\"USD\",\"quantifier\":4}")
            ).andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(is(emptyString())));

            verify(currencyService).update("USD", 4);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldFailIfNotExists() throws Exception {
            when(currencyService.update(anyString(), anyInt())).thenThrow(Failure.class);

            mockMvc.perform(
                    put("/currencies")
                            .contentType(APPLICATION_JSON)
                            .content("{\"code\":\"USD\",\"quantifier\":4}")
            ).andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(is(emptyString())));

            verify(currencyService).update("USD", 4);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldFailIfCodeIsInvalid() throws Exception {
            mockMvc.perform(
                    put("/currencies")
                            .contentType(APPLICATION_JSON)
                            .content("{\"code\":\"U\",\"quantifier\":4}")
            ).andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("code: Currency code must be 3-5 upper letter or numbers"));

            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldFailIfQuantifierIsInvalid() throws Exception {
            mockMvc.perform(
                    put("/currencies")
                            .contentType(APPLICATION_JSON)
                            .content("{\"code\":\"USD\",\"quantifier\":11}")
            ).andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("quantifier: Max quantifier is 10"));

            verifyNoMoreInteractions(mocks);
        }

    }

}
