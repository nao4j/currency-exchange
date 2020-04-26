package com.nao4j.currencyexchange.controller;

import com.nao4j.currencyexchange.convertor.ExchangeToProjectionConverter;
import com.nao4j.currencyexchange.convertor.ExchangeToProjectionConverter.Projection;
import com.nao4j.currencyexchange.projection.ExchangeFull;
import com.nao4j.currencyexchange.service.ExchangeService;
import com.nao4j.currencyexchange.validation.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.status;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/exchanges")
class ExchangeController {

    private final ExchangeService exchangeService;
    private final ExchangeToProjectionConverter exchangeToProjectionConverter;

    @GetMapping("/{from}/{to}")
    ResponseEntity<Object> get(
            @PathVariable @CurrencyCode final String from,
            @PathVariable @CurrencyCode final String to,
            @RequestParam @DateTimeFormat(iso = DATE_TIME) final ZonedDateTime time,
            @RequestParam(defaultValue = "true") final boolean actualOnly,
            @RequestParam(defaultValue = "FULL") final Projection projection
    ) {
        final var localTime = time.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        final var result = actualOnly
                ? exchangeService.getByTime(from, to, localTime)
                : exchangeService.getByTimeNonStrict(from, to, localTime);
        return result.map(exchange -> exchangeToProjectionConverter.convert(exchange, time.getZone(), projection))
                .map(ResponseEntity::ok)
                .orElseGet(() -> notFound().build());
    }

    @PostMapping
    ResponseEntity<Object> create(@RequestBody @Valid final ExchangeFull exchange) {
        final var localTime = exchange.time().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        exchangeService.create(exchange.from(), exchange.to(), exchange.rate(), localTime);
        return status(CREATED).build();
    }

}
