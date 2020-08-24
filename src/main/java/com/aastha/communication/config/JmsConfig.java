package com.aastha.communication.config;

import com.aastha.communication.converter.LoggingMappingJacksonJmsMessageConverter;
import com.aastha.communication.mapping.TypeIdMapping;
import com.aastha.communication.mapping.TypeIdMappingFactory;
import com.aastha.communication.resolver.AasthaDestinationResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.destination.DestinationResolver;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;

import static com.aastha.communication.mapping.TypeIdMapping.JMS_PROPERTY_TYPE_ID;

/**
 * Spring configuration to be unsed by JMS clients and server.
 *
 * @see org.springframework.context.annotation.Import
 * @see com.aastha.communication.client.JmsClientConfig
 * @see com.aastha.communication.server.JmsServerConfig
 */
public class JmsConfig {

    public static final String TOPIC_PREFIX = "topic://";
    public static final String QUEUE_PREFIX = "queue://";

    public static final String VIRTUAL_TOPIC = "VirtualTopic";
    public static final String VIRTUAL_TOPIC_PREFIX = TOPIC_PREFIX + VIRTUAL_TOPIC + ".";
    public static final String CONSUMER = "Consumer";

    @Value("${spring.activemq.broker-url:#{null}}")
    private String brokerUrl;

    @Autowired
    private ObjectMapper json;
    @Autowired(required = false)
    private TypeIdMapping typeIdMapping;

    private MessageConverter jsonMappingMessageConverter;

    @PostConstruct
    public void createMessageConverter() {
        MappingJackson2MessageConverter converter = new LoggingMappingJacksonJmsMessageConverter();
        converter.setObjectMapper(json);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(JMS_PROPERTY_TYPE_ID);
        if (typeIdMapping != null) {
            converter.setTypeIdMappings(typeIdMapping.getMapping());
        }
        jsonMappingMessageConverter = converter;
    }

    @Bean
    @ConditionalOnMissingBean(TypeIdMapping.class)
    public FactoryBean<TypeIdMapping> typeIdMappingFactoryBean() {
        return new TypeIdMappingFactory();
    }

    @Bean
    public DestinationResolver jmsDestinationResolver() {
        return new AasthaDestinationResolver();
    }

    /**
     * Creates a pooled ActiveMQ connection factory for the broker configured by property
     * {@code spring.activemq.broker-url}.
     */
    @Bean(name = "activeMqConnectionFactory")
    public ConnectionFactory jmsConnectionFactory() {
        PooledConnectionFactory bean = new PooledConnectionFactory();

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setRedeliveryPolicy(redeliveryPolicy());
        factory.setBrokerURL(brokerUrl);
        bean.setConnectionFactory(factory);

        return bean;
    }

    @Bean
    public RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setInitialRedeliveryDelay(500);
        redeliveryPolicy.setQueue("dlq.aastha");
        redeliveryPolicy.setMaximumRedeliveries(5);

        return redeliveryPolicy;
    }

    @Bean
    public MessageConverter jsonMappingMessageConverter() {
        return jsonMappingMessageConverter;
    }

    /**
     * Create a destination name for a virtual topic.
     *
     * @see <a href="http://activemq.apache.org/virtual-destinations.html">Virtual Topics</a>
     */
    public static String buildVirtualTopic(String destinationName) {
        return VIRTUAL_TOPIC_PREFIX + destinationName;
    }

    /**
     * Create a destination name for a virtual topic queue of a service.
     *
     * @see <a href="http://activemq.apache.org/virtual-destinations.html">Virtual Topics</a>
     */
    public static String buildVirtualTopicQueue(String service, String destinationName) {
        return QUEUE_PREFIX + CONSUMER + "." + service + "." + VIRTUAL_TOPIC + "."
            + destinationName;
    }

}
