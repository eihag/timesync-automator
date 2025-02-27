package com.timesync.integration.bamboohr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.timesync.integration.bamboohr.model.TimeOffRequest;
import com.timesync.integration.bamboohr.model.TimeOffRequests;
import com.timesync.integration.bamboohr.model.TimeRegistrationEntries;
import com.timesync.integration.bamboohr.model.TimeRegistrationEntry;
import com.timesync.integration.bamboohr.model.TimesheetRegisterClockEntries;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;


@Component
public class BambooRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(BambooRestClient.class);

    @Value("${bamboohr_subdomain}")
    private String subdomain;

    @Value("${bamboohr_apikey}")
    private String apikey;

    @Value("${bamboohr_employee_number}")
    private String employeeNumber;

    @Value("${rest_debug_log:false}")
    private boolean restClientDebug;

    @Value("#{T(java.time.Duration).parse('${rest_client_timeout}')}")
    private Duration restClientTimeout;

    private Client client;
    private String encodedCredentials;

    @PostConstruct
    private void init() {
        client = buildClient(restClientTimeout, restClientTimeout, restClientDebug);
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

    private Client buildClient(Duration clientReadTimeout, Duration clientConnectTimeout, boolean clientDebug) {
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, (int) clientConnectTimeout.toMillis());
        config.property(ClientProperties.READ_TIMEOUT, (int) clientReadTimeout.toMillis());

        ClientBuilder clientBuilder = ClientBuilder
                .newBuilder()
                .withConfig(config);

        if (clientDebug) {
            int logEntrySize = 128 * 1024;
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(this.getClass().toString());
            clientBuilder.register(new LoggingFeature(logger, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_TEXT, logEntrySize));
        }
        Client newClient = clientBuilder.build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        StdDateFormat dateFormat = new StdDateFormat().withColonInTimeZone(false);
        mapper.setDateFormat(dateFormat);

        mapper.registerModule(new JavaTimeModule());

        JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.setMapper(mapper);
        newClient.register(jacksonProvider);

        return newClient;
    }

}
