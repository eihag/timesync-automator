package com.timesync.integration.bamboohr;

import com.timesync.integration.JerseyClientFactory;
import com.timesync.integration.bamboohr.model.TimeOffRequest;
import com.timesync.integration.bamboohr.model.TimeOffRequests;
import com.timesync.integration.bamboohr.model.TimeRegistrationEntries;
import com.timesync.integration.bamboohr.model.TimeRegistrationEntry;
import com.timesync.integration.bamboohr.model.TimesheetRegisterClockEntries;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Component
public class BambooRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(BambooRestClient.class);

    @Value("${bamboohr_subdomain}")
    private String subdomain;

    @Value("${bamboohr_apikey}")
    private String apikey;

    @Value("${bamboohr_employee_number}")
    private String employeeNumber;

    @Inject
    private JerseyClientFactory jerseyClientFactory;

    private Client client;
    private String encodedCredentials;

    @PostConstruct
    private void init() {
        client = jerseyClientFactory.buildClient(getClass());
        encodedCredentials = "Basic " + java.util.Base64.getEncoder().encodeToString((apikey + ":x").getBytes());
    }

    public List<TimeRegistrationEntry> getTimeRegistrationForDatePeriod(LocalDate startDate, LocalDate endDate) {
        return call("/v1/time_tracking/timesheet_entries?start=" + startDate + "&end=" + endDate +
                "&employeeIds=" + employeeNumber, null, HttpMethod.GET, TimeRegistrationEntries.class);
    }

    public void registerTime(TimesheetRegisterClockEntries newEntries) {
        call("/v1/time_tracking/clock_entries/store", Entity.json(newEntries), HttpMethod.POST, String.class);
    }

    public List<TimeOffRequest> getTimeOffRequestForDatePeriod(LocalDate startDate, LocalDate endDate) {
        return call("/v1/time_off/requests?start=" + startDate + "&end=" + endDate +
                "&employeeId=" + employeeNumber, null, HttpMethod.GET, TimeOffRequests.class);
    }

    private <T> T call(String endpoint, Entity<?> entity, String httpMethod, Class<T> clazz) {
        try {
            Invocation.Builder request = this.client.target("https://api.bamboohr.com/api/gateway.php/" + subdomain + endpoint)
                    .request()
                    .header("authorization", encodedCredentials)
                    .header("accept", "application/json");
            Response response = request.method(httpMethod, entity);
            verifyHttpCode(response);
            return response.readEntity(clazz);
        } catch (Exception e) {
            LOG.info("Failed to make HTTP request: {}", e.getMessage());
            throw e;
        }
    }

    private void verifyHttpCode(Response response) {
        if (response.getStatus() != 200 && response.getStatus() != 201) {
            String message = "Unexpected HTTP code: " + response.getStatus();
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }
}
