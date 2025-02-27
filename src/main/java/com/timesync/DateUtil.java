package com.timesync;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public final class DateUtil {

    private DateUtil() {
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            if (dateStr != null && dateStr.length() == 8) {
                dateStr = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
            }
            TemporalAccessor ta = DateTimeFormatter.ISO_DATE.parse(dateStr);
            return LocalDate.from(ta);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse date: '" + dateStr + "'");
        }
    }

}
