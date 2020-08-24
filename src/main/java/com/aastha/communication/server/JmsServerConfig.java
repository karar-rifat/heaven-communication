package com.aastha.communication.server;

import com.aastha.communication.converter.CompatibilityJmsMessageConverter;
import com.aastha.communication.config.JmsConfig;
import com.aastha.communication.json.JsonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.support.SimpleJmsHeaderMapper;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessagingMessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;

import static com.aastha.communication.config.JmsConfig.TOPIC_PREFIX;

/**
 * Spring configuration to be used by JMS server.
 * It provides everything to use {@link org.springframework.jms.annotation.JmsListener}.
 * Not annotated to allow to choose between client, server and mixed config.
 *
 * @see org.springframework.context.annotation.Import
 */
@Import({JsonConfig.class, JmsConfig.class})
@EnableJms
@Slf4j
@Order(100)
public class JmsServerConfig {

    @Value("${aastha.jms.listener.threads:1}")
    private String concurrentThreads;

    @Autowired
    private DestinationResolver destinations;

    @Autowired
    private MessageConverter jsonMappingMessageConverter;

    @Autowired(required = false)
    private ConnectionFactory activeMqConnectionFactory;

    @Value("${aastha.jms.default-message-server:activemq}")
    private String defaultMessageServer;

    @Autowired
    private CompatibilityJmsMessageConverter converter;

    @Autowired(required = false)
    private JmsListenerContainerFactory activeMqJmsListenerContainerFactory;

    @Bean
    public CompatibilityJmsMessageConverter createMessageConverter() {
        JmsServerHeaderMapper headerMapper = new JmsServerHeaderMapper(new SimpleJmsHeaderMapper());
        MessageConverter innerConverter = new MessagingMessageConverter(jsonMappingMessageConverter, headerMapper);
        CompatibilityJmsMessageConverter converter = new CompatibilityJmsMessageConverter(innerConverter, headerMapper);
        return converter;
    }

    @Bean
    //@Profile("activemq")
    public JmsListenerContainerFactory<? extends MessageListenerContainer> activeMqJmsListenerContainerFactory() {
        return jmsListenerContainerFactory(activeMqConnectionFactory);
    }

    @Bean
    @Primary
    public JmsListenerContainerFactory<? extends MessageListenerContainer> jmsListenerContainerFactory() {
        return activeMqJmsListenerContainerFactory;
    }

    private JmsListenerContainerFactory<? extends MessageListenerContainer> jmsListenerContainerFactory(
        ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory bean = new SimpleJmsListenerContainerFactory() {

            @Override
            protected SimpleMessageListenerContainer createContainerInstance() {
                SimpleMessageListenerContainer container = new SimpleMessageListenerContainer() {

                    @Override
                    protected void processMessage(Message message, Session session) {

                        try {
                            super.processMessage(message, session);
                        } finally {
                        }
                    }

                    @Override
                    protected void executeListener(Session session, Message message) {
                        try {
                            super.doExecuteListener(session, message);
                        } catch (Exception e) {
                            ActiveMQMessage amqMessage = (ActiveMQMessage) message;
                            Field redeliveryCounter =
                                ReflectionUtils.findField(ActiveMQMessage.class, "redeliveryCounter");
                            if (redeliveryCounter != null) {
                                redeliveryCounter.setAccessible(true);
                                int redeliveryCount = (int) ReflectionUtils.getField(redeliveryCounter, amqMessage);
                                if (redeliveryCount == 3) {
                                    log.error("Error processing JMS message placing message on DLQ");
                                } else {
                                    log.warn("Error processing JMS message retrying: {}",
                                        ExceptionUtils.getMessage(ExceptionUtils.getRootCause(e)));
                                }
                            } else {
                                log.warn("Error processing JMS message, retrying: {}",
                                    ExceptionUtils.getMessage(ExceptionUtils.getRootCause(e)));
                            }
                        }
                    }

                    @Override
                    protected void doInitialize() throws JMSException {
                        Destination destination = getDestination();
                        if (destination instanceof Topic
                            || (destination == null && getDestinationName() != null && getDestinationName()
                            .startsWith(TOPIC_PREFIX))) {
                            setConcurrency("1");
                        }
                        super.doInitialize();
                    }
                };
                container.setConcurrency(concurrentThreads);
                return container;
            }
        };

        bean.setErrorHandler(t -> {
            if (t instanceof InvalidDestinationException) {
                log.warn(
                    "Error processing JMS message because (temporary) queue has been removed placing message on DLQ",
                    t);
            }
        });

        bean.setDestinationResolver(destinations);
        bean.setMessageConverter(converter);
        bean.setSessionTransacted(true);

        if (connectionFactory instanceof PooledConnectionFactory) {
            PooledConnectionFactory pooledConnectionFactory = (PooledConnectionFactory) connectionFactory;

            setPoolConnectionFactorySettings(bean, pooledConnectionFactory);
            bean.setConnectionFactory(pooledConnectionFactory);
        } else if (connectionFactory instanceof ConnectionFactory) {
            // They override ActiveMQ order and then make the class private so I can't check instance of......
            try {
                Class<?> clazz =
                    Class.forName("org.springframework.cloud.sleuth.instrument.messaging.LazyConnectionFactory");
                Field f = clazz.getDeclaredField("delegate");
                f.setAccessible(true);
                PooledConnectionFactory pooledConnectionFactory =
                    (PooledConnectionFactory) ReflectionUtils.getField(f, connectionFactory);

                setPoolConnectionFactorySettings(bean, pooledConnectionFactory);

                ReflectionUtils.setField(f, connectionFactory, pooledConnectionFactory);

                bean.setConnectionFactory(connectionFactory);
            } catch (Exception e) {
                // Just ignore it for this filthy hack anyway
            }
        } else {
            bean.setConnectionFactory(connectionFactory);
        }

        return bean;
    }

    private PooledConnectionFactory setPoolConnectionFactorySettings(SimpleJmsListenerContainerFactory bean,
        PooledConnectionFactory pooledConnectionFactory) {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(3);
        redeliveryPolicy.setInitialRedeliveryDelay(3000);
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setRedeliveryDelay(30000L);

        ActiveMQConnectionFactory factory =
            (ActiveMQConnectionFactory) pooledConnectionFactory.getConnectionFactory();
        factory.setRedeliveryPolicy(redeliveryPolicy);

        return pooledConnectionFactory;
    }
}
