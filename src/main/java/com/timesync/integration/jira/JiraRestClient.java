package com.timesync.integration.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.timesync.integration.jira.model.IssueList;
import com.timesync.integration.jira.model.WorkLogList;
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
import java.util.logging.Level;


@Component
public class JiraRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(JiraRestClient.class);

    private static final String FIELD_LIST = "&fields=assignee,timetracking,summary&failFast=true";

    @Value("${jira_hostname}")
    private String hostname;

    @Value("${jira_username}")
    private String username;

    @Value("${jira_apikey}")
    private String apikey;

    @Value("${rest_debug_log:false}")
    private boolean restClientDebug;


    @Value("#{T(java.time.Duration).parse('${rest_client_timeout}')}")
    private Duration restClientTimeout;

    private Client client;
    private String encodedCredentials;

    @PostConstruct
    private void init() {
        client = buildClient(restClientTimeout, restClientTimeout, restClientDebug);
        encodedCredentials = "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + apikey).getBytes());
    }

    public IssueList getMyIssuesWithWorkLoggedForDate(LocalDate date) {
        return call("/rest/api/3/search/jql?jql=worklogAuthor='" + username + "'%20AND%20worklogDate='" + date + "'" + FIELD_LIST, null, HttpMethod.GET, IssueList.class);
    }

    public WorkLogList getWorkLogDetails(String issueKey) {
        return call("/rest/api/2/issue/" + issueKey + "/worklog", null, HttpMethod.GET, WorkLogList.class);
    }

    private <T> T call(String endpoint, Entity<?> entity, String httpMethod, Class<T> clazz) {
        try {
            Invocation.Builder request = this.client.target("https://" + hostname + endpoint)
                    .request()
                    .header("authorization", encodedCredentials);
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
