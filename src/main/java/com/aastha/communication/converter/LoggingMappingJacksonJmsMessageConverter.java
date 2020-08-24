package com.aastha.communication.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;

import java.io.IOException;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

@Slf4j
public class LoggingMappingJacksonJmsMessageConverter extends MappingJackson2MessageConverter {
    @Override
    protected TextMessage mapToTextMessage(Object object, Session session,
        ObjectWriter objectWriter) throws JMSException, IOException {
        TextMessage result = super.mapToTextMessage(object, session, objectWriter);
                log.debug("Created text message for {}: '{}'", object.getClass(), result.getText());
        return result;
    }

    @Override
    protected BytesMessage mapToBytesMessage(Object object, Session session,
        ObjectWriter objectWriter) throws JMSException, IOException {
        BytesMessage result = super.mapToBytesMessage(object, session, objectWriter);
                log.debug("Created byte message for {}", object.getClass());
        return result;
    }

    @Override
    protected Object convertFromTextMessage(TextMessage message, JavaType targetJavaType)
        throws JMSException, IOException {
                log.debug("Parse text message '{}' as {}", message.getText(), targetJavaType);
        return super.convertFromTextMessage(message, targetJavaType);
    }

    @Override
    protected Object convertFromBytesMessage(BytesMessage message, JavaType targetJavaType)
        throws JMSException, IOException {
                log.debug("Parse byte message as {}", targetJavaType);
        return super.convertFromBytesMessage(message, targetJavaType);
    }
}
