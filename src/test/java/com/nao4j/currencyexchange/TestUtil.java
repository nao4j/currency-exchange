package com.nao4j.currencyexchange;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoField.OFFSET_SECONDS;

public final class TestUtil {

    private TestUtil() {
        throw new UnsupportedOperationException();
    }

    public static ZoneId defaultZoneOffset(final int hours) {
        return ZoneId.from(ZoneOffset.ofTotalSeconds(ZonedDateTime.now().get(OFFSET_SECONDS) + hours * 3600));
    }

}
