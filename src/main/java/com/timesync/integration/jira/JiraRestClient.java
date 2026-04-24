package com.timesync.integration.jira;

import com.timesync.integration.JerseyClientFactory;
import com.timesync.integration.jira.model.IssueList;
import com.timesync.integration.jira.model.WorkLogList;
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

    @Inject
    private JerseyClientFactory jerseyClientFactory;

    private Client client;
    private String encodedCredentials;

    @PostConstruct
    private void init() {
        client = jerseyClientFactory.buildClient(getClass());
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
}
