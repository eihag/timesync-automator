package com.timesync.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JerseyClientFactory {

    private static final int DEBUG_LOG_ENTRY_SIZE = 128 * 1024;

    @Value("${rest_debug_log:false}")
    private boolean restDebug;

    @Value("#{T(java.time.Duration).parse('${rest_client_timeout}')}")
    private Duration restTimeout;

    public Client buildClient(Class<?> ownerClass) {
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, (int) restTimeout.toMillis());
        config.property(ClientProperties.READ_TIMEOUT, (int) restTimeout.toMillis());

        ClientBuilder clientBuilder = ClientBuilder.newBuilder().withConfig(config);

        if (restDebug) {
            Logger logger = Logger.getLogger(ownerClass.toString());
            clientBuilder.register(new LoggingFeature(logger, Level.INFO,
                    LoggingFeature.Verbosity.PAYLOAD_TEXT, DEBUG_LOG_ENTRY_SIZE));
        }

        Client newClient = clientBuilder.build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(false));
        mapper.registerModule(new JavaTimeModule());

        JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.setMapper(mapper);
        newClient.register(jacksonProvider);

        return newClient;
    }
}
