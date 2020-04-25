package com.nao4j.currencyexchange.controller;

import com.nao4j.currencyexchange.convertor.CurrencyToProjectionConverter;
import com.nao4j.currencyexchange.convertor.CurrencyToProjectionConverter.Projection;
import com.nao4j.currencyexchange.exception.Failure;
import com.nao4j.currencyexchange.projection.CurrencyFull;
import com.nao4j.currencyexchange.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
@RequestMapping("/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;
    private final CurrencyToProjectionConverter currencyToProjectionConverter;

    @GetMapping
    public List<Object> getAll(
            @RequestParam @DateTimeFormat(iso = DATE_TIME) final ZonedDateTime time,
            @RequestParam(defaultValue = "true") final boolean actualOnly,
            @RequestParam(defaultValue = "FULL") final Projection projection
    ) {
        final var localTime = time.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        final var result = actualOnly
                ? currencyService.getAllByTime(localTime)
                : currencyService.getAllByTimeNonStrict(localTime);
        return result.stream()
                .map(currency -> currencyToProjectionConverter.convert(currency, projection))
                .collect(toList());
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody CurrencyFull currency) {
        try {
            currencyService.create(currency.code(), currency.quantifier());
            return status(CREATED).build();
        } catch (Failure failure) {
            return status(CONFLICT).build();
        }
    }

    @PutMapping
    public ResponseEntity<Object> update(@RequestBody final CurrencyFull currency) {
        try {
            currencyService.update(currency.code(), currency.quantifier());
            return noContent().build();
        } catch (Failure failure) {
            return notFound().build();
        }
    }

}
