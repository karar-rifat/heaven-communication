package com.aastha.communication.message.listener;

import com.aastha.communication.config.JmsConfig;
import com.aastha.communication.message.dto.SimpleRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SimpleMessageListener {

    private static final String SIMPLE_REQUEST_GENERATE = "simple-request-generate";

    @JmsListener(
            destination = JmsConfig.TOPIC_PREFIX + JmsConfig.VIRTUAL_TOPIC
                    + "." + SIMPLE_REQUEST_GENERATE)
    public void processAsyncMessageFromTopic(String message) {
        System.out.println(message);
    }

    @JmsListener(
            destination = JmsConfig.QUEUE_PREFIX +JmsConfig.CONSUMER+ ".Aastha."
                    + JmsConfig.VIRTUAL_TOPIC
                    + "." + SIMPLE_REQUEST_GENERATE)
    public void processAsyncMessageFromQueue(String message) {
        System.out.println(message);
    }


}
