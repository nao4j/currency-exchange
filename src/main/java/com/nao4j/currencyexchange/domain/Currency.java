package com.nao4j.currencyexchange.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import static javax.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Data
@Entity
@Immutable
@RequiredArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
@Table(name = "currencies", schema = "currency_exchange")
public class Currency {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(generator = "currencies_id_generator", strategy = SEQUENCE)
    @SequenceGenerator(
            name = "currencies_id_generator",
            sequenceName = "currencies_id_seq",
            schema = "currency_exchange",
            allocationSize = 1
    )
    private final Long id;

    @Column(name = "code", length = 3, nullable = false, unique = true)
    private final String code;

    @Column(name = "quantifier", nullable = false)
    private final int quantifier;

    public Currency(final String code, int quantifier) {
        id = null;
        this.code = code;
        this.quantifier = quantifier;
    }
}
