package com.aastha.communication.message.producer;

import com.aastha.communication.config.JmsConfig;
import com.aastha.communication.message.dto.SimpleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimpleMessageProducer {

    @Autowired
    private JmsMessagingTemplate messagingTemplate;

    private static final String SIMPLE_REQUEST_GENERATE = "simple-request-generate";

    public static final String TOPICDESTINATION =
            JmsConfig.buildVirtualTopic(SIMPLE_REQUEST_GENERATE);

    public static final String QUEUEDESTINATION =
            JmsConfig.buildVirtualTopicQueue("Aastha",SIMPLE_REQUEST_GENERATE);

    public void sendMessageToTopic(String request) {
        messagingTemplate.convertAndSend(TOPICDESTINATION, request);
    }

    public void sendMessageToQueue(String request) {
        messagingTemplate.convertAndSend(QUEUEDESTINATION, request);
    }

}
