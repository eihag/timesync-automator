package com.timesync;

import com.timesync.service.TimeSyncService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.Year;
import java.util.Calendar;

@SpringBootTest
class TimeSyncServiceTest {

    private final int year = Year.now().getValue();
    private final LocalDate startOfYear = LocalDate.of(year, 1, 1);

    @Inject
    private TimeSyncService timeSyncService;


    @Test
    void testReportTimeLogged() {
        timeSyncService.reportJiraAndBamboo(startOfYear, LocalDate.of(year, 2, 28), true);
    }

    @Test
    void testReportTimeLoggedFullYear() {
        timeSyncService.reportJiraAndBamboo(startOfYear, LocalDate.of(year, 12, 31), true);
    }

    @Test
    void testReportTimeLoggedWithBamboo() {
        timeSyncService.reportJiraAndBamboo(startOfYear, LocalDate.of(year, 1, 14), false);
    }

    @Test
    void testlogTimed() {
        timeSyncService.logBambooWork(startOfYear, LocalDate.of(year, 2, 28), true, true);
    }

    private String getYear() {
        return "" + Calendar.getInstance().get(Calendar.YEAR);
    }

}
