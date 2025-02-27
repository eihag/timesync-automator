package com.timesync;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateUtilTest {

    @ParameterizedTest
    @CsvSource({"2024-02-14"})
    void parseDate(String dateStr) {
        assertNotNull(DateUtil.parseDate(dateStr));
    }

    @ParameterizedTest
    @CsvSource({"2024-13-1", "2024-02-31"})
    void parseBadDate(String dateStr) {
        assertThrows(IllegalArgumentException.class, () -> DateUtil.parseDate(dateStr));
    }
}