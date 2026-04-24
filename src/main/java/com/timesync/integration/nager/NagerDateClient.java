package com.timesync.integration.nager;

import com.timesync.DateUtil;
import com.timesync.integration.JerseyClientFactory;
import com.timesync.integration.nager.model.NagerHolidays;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class NagerDateClient {
    private static final Logger LOG = LoggerFactory.getLogger(NagerDateClient.class);

    @Value("${nager_base_url}")
    private String baseUrl;

    @Value("${holidays_country_code}")
    private String countryCode;

    @Inject
    private JerseyClientFactory jerseyClientFactory;

    private Client client;

    @PostConstruct
    private void init() {
        client = jerseyClientFactory.buildClient(getClass());
    }

    public Set<LocalDate> getHolidayDatesForYear(int year) {
        String url = baseUrl + "/" + year + "/" + countryCode;
        try {
            Response response = client.target(url).request().accept("application/json").get();
            if (response.getStatus() != 200) {
                String message = "Unexpected HTTP code " + response.getStatus() + " from " + url;
                LOG.error(message);
                throw new RuntimeException(message);
            }
            NagerHolidays holidays = response.readEntity(NagerHolidays.class);
            return holidays.stream()
                    .map(h -> DateUtil.parseDate(h.getDate()))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            LOG.info("Failed to fetch holidays from {}: {}", url, e.getMessage());
            throw e;
        }
    }
}
