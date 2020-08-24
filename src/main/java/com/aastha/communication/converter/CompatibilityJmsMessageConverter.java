package com.aastha.communication.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.support.JmsHeaderMapper;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import java.io.Serializable;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import static org.springframework.messaging.support.MessageBuilder.withPayload;

/**
 * For compatibility this wrapper handles all non Spring {@link org.springframework.messaging.Message}
 * or JMS {@link ObjectMessage} before delegating.
 */
@Slf4j
public class CompatibilityJmsMessageConverter implements MessageConverter {

    private final MessageConverter delegate;
    private final JmsHeaderMapper headerMapper;
    @Value("${aastha.jms.deprecatedAsJson:false}")
    boolean transferDeprecatedAsJson;

    public CompatibilityJmsMessageConverter(MessageConverter delegate, JmsHeaderMapper headerMapper) {
        this.delegate = delegate;
        this.headerMapper = headerMapper;
    }

    /**
     * Everything except {@link Message JMS messages} is converted to Spring
     * {@link org.springframework.messaging.Message} before delegating.
     */
    @Override
    public Message toMessage(Object object, Session session)
        throws JMSException, MessageConversionException {
        if (object instanceof Message) {
            log.debug("return {} unchanged", object.getClass());
            return (Message) object;
        }
        if (!transferDeprecatedAsJson) {
            log.debug("convert {} to object message", object.getClass());
            return session.createObjectMessage((Serializable) object);
        }
        org.springframework.messaging.Message<?> message;
        if (object instanceof org.springframework.messaging.Message) {
            message = (org.springframework.messaging.Message<?>) object;
        } else {
            log.debug("convert {} to Spring message", object.getClass());
            message = withPayload(object).build();
        }
        return delegate.toMessage(message, session);
    }

    /**
     * Any {@link ObjectMessage} will be converted directly by extracting the object as payload,
     * others are converted by delegation.
     */
    @Override
    public org.springframework.messaging.Message<?> fromMessage(Message message)
        throws JMSException, MessageConversionException {
        if (message instanceof ObjectMessage) {
            Serializable result = ((ObjectMessage) message).getObject();
            log.debug("convert payload {} of object message", result.getClass());
            return withPayload(result)
                .copyHeaders(headerMapper.toHeaders(message)).build();
        }
        return (org.springframework.messaging.Message<?>) delegate.fromMessage(message);
    }
}
