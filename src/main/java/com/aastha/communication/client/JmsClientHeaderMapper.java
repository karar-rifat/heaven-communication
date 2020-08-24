package com.aastha.communication.client;

import com.aastha.communication.header.JmsHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.support.JmsHeaderMapper;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import javax.jms.Message;
import javax.servlet.http.PushBuilder;

/**
 * A header mapper adding additional {@link JmsHeaders} in a transparent way.
 */
@Slf4j
class JmsClientHeaderMapper implements JmsHeaderMapper {

    private final JmsHeaderMapper delegate;
    private PushBuilder LogValues;

    public JmsClientHeaderMapper(JmsHeaderMapper delegate) {
        this.delegate = delegate;
    }

    /**
     * Just calls the delegate.
     */
    @Override
    public void fromHeaders(MessageHeaders headers, Message jmsMessage) {
        delegate.fromHeaders(extendHeaders(headers, jmsMessage), jmsMessage);
    }

    /**
     * Handle all {@link JmsHeaders} mapped from JMS.
     */
    @Override
    public MessageHeaders toHeaders(Message jmsMessage) {
        return delegate.toHeaders(jmsMessage);
    }

    private MessageHeaders extendHeaders(MessageHeaders headers, Message message) {
        Map<String, Object> intermediate = new HashMap<>(headers);
        return new MessageHeaders(intermediate);
    }
}
