package com.nao4j.currencyexchange.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Data
@Entity
@Immutable
@RequiredArgsConstructor
@NoArgsConstructor(force = true, access = PRIVATE)
@Table(name = "exchanges", schema = "currency_exchange")
public class Exchange {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(generator = "exchanges_id_generator", strategy = SEQUENCE)
    @SequenceGenerator(
            name = "exchanges_id_generator",
            sequenceName = "exchanges_id_seq",
            schema = "currency_exchange",
            allocationSize = 1
    )
    private final Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "from_id", nullable = false, unique = true)
    private final Currency from;

    @OneToOne(optional = false)
    @JoinColumn(name = "to_id", nullable = false, unique = true)
    private final Currency to;

    @Column(name = "rate", length = 19, scale = 10, nullable = false)
    private final BigDecimal rate;

    @Column(name = "time", nullable = false)
    private final LocalDateTime time;

    public Exchange(final Currency from, final Currency to, final BigDecimal rate, final LocalDateTime time) {
        id = null;
        this.from = from;
        this.to = to;
        this.rate = rate;
        this.time = time;
    }

}
