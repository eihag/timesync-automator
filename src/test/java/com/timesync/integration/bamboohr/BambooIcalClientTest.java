package com.timesync.integration.bamboohr;

import com.timesync.integration.bamboohr.model.CompanyHolidaySimpleDto;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BambooIcalClientTest {

    @Inject
    private BambooIcalClient bambooIcalClient;

    @Test
    void getCompanyHolidays() {
        List<CompanyHolidaySimpleDto> companyHolidays = bambooIcalClient.getCompanyHolidays();
        assertNotNull(companyHolidays);
        for (CompanyHolidaySimpleDto holiday : companyHolidays) {
            System.out.println(holiday);
        }
    }

}