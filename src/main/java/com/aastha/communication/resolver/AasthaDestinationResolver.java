package com.aastha.communication.resolver;

import org.springframework.jms.support.destination.DestinationResolver;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import static com.aastha.communication.config.JmsConfig.QUEUE_PREFIX;
import static com.aastha.communication.config.JmsConfig.TOPIC_PREFIX;

public class AasthaDestinationResolver implements DestinationResolver {

    @Override
    public Destination resolveDestinationName(Session session, String destinationName,
        boolean pubSubDomain) throws JMSException {
        if (destinationName.startsWith(TOPIC_PREFIX)) {
            destinationName = destinationName.substring(TOPIC_PREFIX.length());
            pubSubDomain = true;
        }
        if (destinationName.startsWith(QUEUE_PREFIX)) {
            destinationName = destinationName.substring(QUEUE_PREFIX.length());
            pubSubDomain = false;
        }
        if (pubSubDomain) {
            System.out.println("Creating topic");
            return session.createTopic(destinationName);
        }

        System.out.println("Creating queue for destination");

        return session.createQueue(destinationName);
    }
}
