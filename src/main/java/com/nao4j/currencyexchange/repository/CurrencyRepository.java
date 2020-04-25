package com.nao4j.currencyexchange.repository;

import com.nao4j.currencyexchange.domain.Currency;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends Repository<Currency, Long> {

    boolean existsByCode(String code);

    Optional<Currency> findByCode(String code);

    @Query("""
        SELECT DISTINCT c
        FROM Currency c
        JOIN Exchange e ON c.id = e.from.id OR c.id = e.to.id
        WHERE e.time BETWEEN :start AND :end
        ORDER BY c.code
    """)
    List<Currency> findAllActualInPeriod(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT DISTINCT c
        FROM Currency c
        JOIN Exchange e ON c.id = e.from.id OR c.id = e.to.id
        WHERE e.time <= :time
        ORDER BY c.code
    """)
    List<Currency> findAllUpToTime(LocalDateTime time);

    Currency save(Currency currency);

}
