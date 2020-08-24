package com.aastha.communication.server;

import com.aastha.communication.header.JmsHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.support.JmsHeaderMapper;
import org.springframework.messaging.MessageHeaders;

import javax.jms.Message;

/**
 * A header mapper using additional {@link JmsHeaders} in a transparent way.
 */
@Slf4j
class JmsServerHeaderMapper implements JmsHeaderMapper {

    private final JmsHeaderMapper delegate;

    JmsServerHeaderMapper(JmsHeaderMapper delegate) {
        this.delegate = delegate;
    }

    /**
     * Just calls the delegate.
     */
    @Override
    public void fromHeaders(MessageHeaders headers, Message jmsMessage) {
        delegate.fromHeaders(headers, jmsMessage);
    }

    /**
     * Handle all {@link JmsHeaders} mapped from JMS.
     */
    @Override
    public MessageHeaders toHeaders(Message jmsMessage) {
        return useHeaders(delegate.toHeaders(jmsMessage), jmsMessage);
    }

    private MessageHeaders useHeaders(MessageHeaders headers, Message message) {
        //        ofNullable(headers.get(JmsHeaders.HEADER_SESSION_ID, String.class))
        //            .ifPresent(LogValues::setSessionId); //add headers if needed
        return headers;
    }
}
