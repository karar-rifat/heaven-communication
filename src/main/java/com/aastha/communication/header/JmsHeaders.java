package com.aastha.communication.header;

import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * Typical JMS message headers used by the system.
 *
 */
public interface JmsHeaders {
    String HEADER_TOKEN = "AasthaToken";

    /**
     * Helper method creating a token header only. Useful for passing an alternative token to a JMS server.
     *
     * @return a map with a single entry named {@link #HEADER_TOKEN}
     * @since 1.0.8
     */
    static Map<String, Object> tokenHeader(String token) {
        return singletonMap(HEADER_TOKEN, token);
    }
}
