package com.aastha.communication.client;

import com.aastha.communication.converter.CompatibilityJmsMessageConverter;
import com.aastha.communication.config.JmsConfig;
import com.aastha.communication.json.JsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.SimpleJmsHeaderMapper;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessagingMessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.ConnectionFactory;

/**
 * Spring configuration to be used by JMS clients. It provides fully configured
 * {@link JmsOperations}. Not annotated to allow to choose between client, server and mixed config.
 * A timeout should be configured by setting {@code heaven.jms.timeout} to a number of milliseconds.
 *
 * @see org.springframework.context.annotation.Import
 */
@Import({JsonConfig.class, JmsConfig.class})
public class JmsClientConfig {

    @Value("${aastha.jms.timeout:0}")
    private long receiveTimeout;

    @Autowired
    private DestinationResolver destinations;

    @Autowired
    private MessageConverter jsonMappingMessageConverter;

    @Autowired
    @Qualifier("clientCompatibilityJmsMessageConverter")
    private CompatibilityJmsMessageConverter converter;

    @Autowired(required = false)
    @Qualifier("activeMqConnectionFactory")
    private ConnectionFactory activeMqConnectionFactory;

    @Autowired(required = false)
    @Qualifier("activeMqJmsTemplate")
    private JmsTemplate activeMqJmsTemplate;

    @Autowired(required = false)
    @Qualifier("activeMqMessagingTemplate")
    private JmsMessagingTemplate activeMqMessagingTemplate;

    @Bean("clientCompatibilityJmsMessageConverter")
    public CompatibilityJmsMessageConverter createMessageConverter() {
        JmsClientHeaderMapper headerMapper = new JmsClientHeaderMapper(new SimpleJmsHeaderMapper());
        MessageConverter innerConverter = new MessagingMessageConverter(jsonMappingMessageConverter, headerMapper);
        CompatibilityJmsMessageConverter converter = new CompatibilityJmsMessageConverter(innerConverter, headerMapper);

        return converter;
    }

    @Bean(name = "activeMqMessagingTemplate")
    public JmsMessagingTemplate activeMqMessagingTemplate() {
        JmsMessagingTemplate bean = new JmsMessagingTemplate(activeMqConnectionFactory);
        bean.setJmsMessageConverter(converter);
        bean.setJmsTemplate(activeMqJmsTemplate);
        return bean;
    }

    @Bean(name = "activeMqJmsTemplate")
    public JmsTemplate activeMqJmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(activeMqConnectionFactory);
        jmsTemplate.setMessageConverter(converter);
        jmsTemplate.setDestinationResolver(destinations);
        jmsTemplate.setReceiveTimeout(receiveTimeout);
        return jmsTemplate;
    }

    @Bean(name = "aasthaJmsOperations")
    @Primary
    public JmsTemplate jmsTemplate() {
        return activeMqJmsTemplate;
    }

    @Bean
    @Primary
    public JmsMessagingTemplate jmsMessagingTemplate() {
        return activeMqMessagingTemplate;
    }
}
