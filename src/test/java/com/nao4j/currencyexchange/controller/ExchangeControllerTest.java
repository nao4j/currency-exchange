package com.nao4j.currencyexchange.controller;

import com.nao4j.currencyexchange.converter.ExchangeToProjectionConverter;
import com.nao4j.currencyexchange.converter.ExchangeToProjectionConverter.Projection;
import com.nao4j.currencyexchange.domain.Exchange;
import com.nao4j.currencyexchange.projection.ExchangeFull;
import com.nao4j.currencyexchange.projection.ExchangeShort;
import com.nao4j.currencyexchange.service.ExchangeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.nao4j.currencyexchange.TestUtil.defaultZoneOffset;
import static com.nao4j.currencyexchange.converter.ExchangeToProjectionConverter.Projection.FULL;
import static com.nao4j.currencyexchange.converter.ExchangeToProjectionConverter.Projection.SHORT;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeController.class)
class ExchangeControllerTest {

    @MockBean
    ExchangeService exchangeService;
    @MockBean
    ExchangeToProjectionConverter exchangeToProjectionConverter;
    @Autowired
    MockMvc mockMvc;

    Object[] mocks;

    @BeforeEach
    void setup() {
        mocks = new Object[] {exchangeService, exchangeToProjectionConverter};
    }

    @AfterEach
    void cleanup() {
        // workaround: for use @MockBean with @Nested annotations
        reset(mocks);
    }

    @Nested
    class Get {

        @Test
        void shouldPassForFullActual() throws Exception {
            final var from = "USD";
            final var to = "RUB";
            final var rate = new BigDecimal("73.2");
            final var zone = defaultZoneOffset(-1);
            final var time = ZonedDateTime.of(LocalDateTime.parse("2020-04-30T14:38"), zone);
            final var exchange = mock(Exchange.class);
            when(exchangeService.getByTime(anyString(), anyString(), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(exchange));
            when(exchangeToProjectionConverter.convert(any(Exchange.class), any(ZoneId.class), any(Projection.class)))
                    .thenReturn(new ExchangeFull(from, to, rate, time));

            mockMvc.perform(get("/exchanges/{from}/{to}", from, to).param("time", time.format(ISO_DATE_TIME)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    // workaround: for compare zoned date time in json
                    .andExpect(jsonPath("$.*", hasSize(4)))
                    .andExpect(jsonPath("$.from").value("USD"))
                    .andExpect(jsonPath("$.to").value("RUB"))
                    .andExpect(jsonPath("$.rate").value("73.2"))
                    .andExpect(jsonPath("$.time").value(time.format(ISO_ZONED_DATE_TIME)));

            verify(exchangeService).getByTime(from, to, LocalDateTime.parse("2020-04-30T15:38"));
            verify(exchangeToProjectionConverter).convert(exchange, zone, FULL);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldPassForShortActual() throws Exception {
            final var from = "USD";
            final var to = "RUB";
            final var rate = new BigDecimal("73.2");
            final var zone = defaultZoneOffset(-1);
            final var time = ZonedDateTime.of(LocalDateTime.parse("2020-04-30T14:38"), zone);
            final var exchange = mock(Exchange.class);
            when(exchangeService.getByTime(anyString(), anyString(), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(exchange));
            when(exchangeToProjectionConverter.convert(any(Exchange.class), any(ZoneId.class), any(Projection.class)))
                    .thenReturn(new ExchangeShort(rate, time));

            mockMvc.perform(
                    get("/exchanges/{from}/{to}", from, to)
                            .param("time", time.format(ISO_DATE_TIME))
                            .param("projection", "SHORT")
            ).andDo(print())
                    .andExpect(status().isOk())
                    // workaround: for compare zoned date time in json
                    .andExpect(jsonPath("$.*", hasSize(2)))
                    .andExpect(jsonPath("$.rate").value("73.2"))
                    .andExpect(jsonPath("$.time").value(time.format(ISO_ZONED_DATE_TIME)));

            verify(exchangeService).getByTime(from, to, LocalDateTime.parse("2020-04-30T15:38"));
            verify(exchangeToProjectionConverter).convert(exchange, zone, SHORT);
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldPassForFullNonStrict() throws Exception {
            //TODO: реализовать тест
        }

        @Test
        void shouldPassForShortNonStrict() throws Exception {
            //TODO: реализовать тест
        }

        @Test
        void shouldFailIfTimeNotProvided() throws Exception {
            //TODO: реализовать тест
        }

        @Test
        void shouldFailIfProjectionIsNotSupported() throws Exception {
            //TODO: реализовать тест
        }

        //TODO: реализовать тесты

    }

    @Nested
    class Create {

        @Test
        void shouldPass() throws Exception {
            final var from = "USD";
            final var to = "RUB";
            final var rate = new BigDecimal("73.49");
            final var zone = defaultZoneOffset(0);
            final var time = ZonedDateTime.of(LocalDateTime.parse("2020-04-30T14:38"), zone);

            mockMvc.perform(
                    post("/exchanges")
                            .contentType(APPLICATION_JSON)
                            .content(format("""
                                {"from": "%s", "to": "%s", "rate": %s, "time": "%s"}
                            """, from, to, rate, time.format(ISO_DATE_TIME)))
            ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string(is(emptyString())));

            verify(exchangeService).create(from, to, rate, LocalDateTime.parse("2020-04-30T14:38"));
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldPassIfZoneIsNotDefault() throws Exception {
            final var from = "USD";
            final var to = "RUB";
            final var rate = new BigDecimal("73.49");
            final var zone = defaultZoneOffset(-3);
            final var time = ZonedDateTime.of(LocalDateTime.parse("2020-04-30T14:38"), zone);

            mockMvc.perform(
                    post("/exchanges")
                            .contentType(APPLICATION_JSON)
                            .content(format("""
                                {"from": "%s", "to": "%s", "rate": %s, "time": "%s"}
                            """, from, to, rate, time.format(ISO_DATE_TIME)))
            ).andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().string(is(emptyString())));

            verify(exchangeService).create(from, to, rate, LocalDateTime.parse("2020-04-30T17:38"));
            verifyNoMoreInteractions(mocks);
        }

        @Test
        void shouldFailIfBodyIsInvalid() throws Exception {
            mockMvc.perform(
                    post("/exchanges")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                {"from": "R", "to": "USDUSD", "rate": 0.00000000001, "time": "2020-05-17T11:10+03:00"}
                            """)
            ).andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString(
                            "to: Currency code must be 3-5 upper letter or numbers"
                    ))).andExpect(jsonPath("$.message").value(containsString(
                            "from: Currency code must be 3-5 upper letter or numbers"
                    ))).andExpect(jsonPath("$.message").value(containsString(
                            "rate: Rate must match format 0000000000000000000.0000000000"
                    )));

            verifyNoMoreInteractions(mocks);
        }

    }

}
