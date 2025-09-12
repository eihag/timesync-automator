package com.timesync.integration.bamboohr;

import com.timesync.DateUtil;
import com.timesync.integration.bamboohr.model.TimeOffRequest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(properties = "rest_debug_log:true")
class BambooRestClientTest {

    @Inject
    private BambooRestClient bambooRestClient;

    @Test
    void testTimeOffRequests() {
        List<TimeOffRequest> timeOffRequests = bambooRestClient.getTimeOffRequestForDatePeriod(DateUtil.parseDate("2025-01-01"), DateUtil.parseDate("2025-12-31"));
        if (timeOffRequests != null) {
            timeOffRequests.forEach(System.out::println);
        }
    }

}
