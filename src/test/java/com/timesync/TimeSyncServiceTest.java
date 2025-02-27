package com.timesync;

import com.timesync.service.TimeSyncService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TimeSyncServiceTest {

    @Inject
    private TimeSyncService timeSyncService;

    @Test
    void testReportTimeLogged() {
        timeSyncService.reportJiraAndBamboo(DateUtil.parseDate("2025-01-01"), DateUtil.parseDate("2025-02-28"), true);
    }

    @Test
    void testReportTimeLoggedFullYear() {
        timeSyncService.reportJiraAndBamboo(DateUtil.parseDate("2025-01-01"), DateUtil.parseDate("2025-12-31"), true);
    }

    @Test
    void testReportTimeLoggedWithBamboo() {
        timeSyncService.reportJiraAndBamboo(DateUtil.parseDate("2025-01-01"), DateUtil.parseDate("2025-01-14"), false);
    }

    @Test
    void testlogTimed() {
        timeSyncService.logBambooWork(DateUtil.parseDate("2025-01-01"), DateUtil.parseDate("2025-02-28"), true, true);
    }

}
