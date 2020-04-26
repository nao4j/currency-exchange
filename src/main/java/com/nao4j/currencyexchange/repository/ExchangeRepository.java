package com.nao4j.currencyexchange.repository;

import com.nao4j.currencyexchange.domain.Exchange;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface ExchangeRepository extends Repository<Exchange, Long> {

    Optional<Exchange> findFirstByFromCodeAndToCodeAndTimeBetweenOrderByTimeDesc(
            String from, String to, LocalDateTime start, LocalDateTime end
    );

    Collection<Exchange> findTop1000ByToCodeAndTimeBetweenOrderByTimeDesc(
            String to,
            LocalDateTime start,
            LocalDateTime end
    );

    Exchange saveAndFlush(Exchange exchange);

}
